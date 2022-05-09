/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.manager.client.api.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.inlong.manager.client.api.InlongGroup;
import org.apache.inlong.manager.client.api.InlongGroupConf;
import org.apache.inlong.manager.client.api.InlongGroupContext;
import org.apache.inlong.manager.client.api.InlongGroupContext.InlongGroupState;
import org.apache.inlong.manager.client.api.InlongStream;
import org.apache.inlong.manager.client.api.InlongStreamBuilder;
import org.apache.inlong.manager.client.api.InlongStreamConf;
import org.apache.inlong.manager.client.api.inner.InnerGroupContext;
import org.apache.inlong.manager.client.api.inner.InnerInlongManagerClient;
import org.apache.inlong.manager.client.api.util.AssertUtil;
import org.apache.inlong.manager.client.api.util.GsonUtil;
import org.apache.inlong.manager.client.api.util.InlongGroupTransfer;
import org.apache.inlong.manager.client.api.util.InlongParser;
import org.apache.inlong.manager.common.enums.GroupStatus;
import org.apache.inlong.manager.common.enums.ProcessStatus;
import org.apache.inlong.manager.common.pojo.group.InlongGroupApproveRequest;
import org.apache.inlong.manager.common.pojo.group.InlongGroupInfo;
import org.apache.inlong.manager.common.pojo.group.InlongGroupRequest;
import org.apache.inlong.manager.common.pojo.group.InlongGroupResponse;
import org.apache.inlong.manager.common.pojo.stream.FullStreamResponse;
import org.apache.inlong.manager.common.pojo.stream.InlongStreamApproveRequest;
import org.apache.inlong.manager.common.pojo.stream.InlongStreamConfigLogListResponse;
import org.apache.inlong.manager.common.pojo.workflow.EventLogView;
import org.apache.inlong.manager.common.pojo.workflow.ProcessResponse;
import org.apache.inlong.manager.common.pojo.workflow.TaskResponse;
import org.apache.inlong.manager.common.pojo.workflow.WorkflowResult;
import org.apache.inlong.manager.common.util.CommonBeanUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InlongGroupImpl implements InlongGroup {

    private InlongGroupConf groupConf;

    private InnerInlongManagerClient managerClient;

    private InnerGroupContext groupContext;

    public InlongGroupImpl(InlongGroupConf groupConf, InlongClientImpl inlongClient) {
        this.groupConf = groupConf;
        this.groupContext = new InnerGroupContext();
        InlongGroupInfo groupInfo = InlongGroupTransfer.createGroupInfo(groupConf);
        this.groupContext.setGroupInfo(groupInfo);
        if (this.managerClient == null) {
            this.managerClient = new InnerInlongManagerClient(inlongClient.getConfiguration());
        }
        InlongGroupRequest inlongGroupRequest = groupInfo.genRequest();
        Pair<Boolean, InlongGroupResponse> existMsg = managerClient.isGroupExists(inlongGroupRequest);
        if (existMsg.getKey()) {
            //Update current snapshot
            groupInfo = CommonBeanUtils.copyProperties(existMsg.getValue(), InlongGroupInfo::new);
            this.groupContext.setGroupInfo(groupInfo);
        } else {
            String groupId = managerClient.createGroup(inlongGroupRequest);
            groupInfo.setInlongGroupId(groupId);
        }
    }

    @Override
    public InlongStreamBuilder createStream(InlongStreamConf dataStreamConf) throws Exception {
        return new DefaultInlongStreamBuilder(dataStreamConf, this.groupContext, this.managerClient);
    }

    @Override
    public InlongGroupContext context() throws Exception {
        return generateSnapshot();
    }

    @Override
    public InlongGroupContext init() throws Exception {
        InlongGroupInfo groupInfo = this.groupContext.getGroupInfo();
        WorkflowResult initWorkflowResult = managerClient.initInlongGroup(groupInfo.genRequest());
        List<TaskResponse> taskViews = initWorkflowResult.getNewTasks();
        AssertUtil.notEmpty(taskViews, "Init business info failed");
        TaskResponse taskView = taskViews.get(0);
        final int taskId = taskView.getId();
        ProcessResponse processView = initWorkflowResult.getProcessInfo();
        AssertUtil.isTrue(ProcessStatus.PROCESSING == processView.getStatus(),
                String.format("Business info state : %s is not corrected , should be PROCESSING",
                        processView.getStatus()));
        String formData = GsonUtil.toJson(processView.getFormData());
        Pair<InlongGroupApproveRequest, List<InlongStreamApproveRequest>> initMsg = InlongParser
                .parseGroupForm(formData);
        groupContext.setInitMsg(initMsg);
        WorkflowResult startWorkflowResult = managerClient.startInlongGroup(taskId, initMsg);
        processView = startWorkflowResult.getProcessInfo();
        AssertUtil.isTrue(ProcessStatus.COMPLETED == processView.getStatus(),
                String.format("Business info state : %s is not corrected , should be COMPLETED",
                        processView.getStatus()));
        return generateSnapshot();
    }

    @Override
    public void update(InlongGroupConf conf) throws Exception {
        if (conf != null) {
            AssertUtil.isTrue(conf.getGroupName() != null
                            && conf.getGroupName().equals(this.groupConf.getGroupName()),
                    "Group must have same name");
            this.groupConf = conf;
        } else {
            conf = this.groupConf;
        }
        final String groupId = "b_" + conf.getGroupName();
        InlongGroupResponse groupResponse = managerClient.getGroupInfo(groupId);
        InlongGroupState state = InlongGroupState.parseByBizStatus(groupResponse.getStatus());
        AssertUtil.isTrue(state != InlongGroupState.INITIALIZING,
                "Inlong Group is in init state, should not be updated");
        InlongGroupInfo groupInfo = InlongGroupTransfer.createGroupInfo(conf);
        InlongGroupRequest groupRequest = groupInfo.genRequest();
        Pair<String, String> idAndErr = managerClient.updateGroup(groupRequest);
        this.groupContext.setGroupInfo(groupInfo);
        String errMsg = idAndErr.getValue();
        AssertUtil.isNull(errMsg, errMsg);
    }

    @Override
    public InlongGroupContext reInitOnUpdate(InlongGroupConf conf) throws Exception {
        return initOnUpdate(conf);
    }

    @Override
    public InlongGroupContext initOnUpdate(InlongGroupConf conf) throws Exception {
        update(conf);
        InlongGroupInfo groupInfo = InlongGroupTransfer.createGroupInfo(conf);
        InlongGroupRequest groupRequest = groupInfo.genRequest();
        Pair<Boolean, InlongGroupResponse> existMsg = managerClient.isGroupExists(groupRequest);
        if (existMsg.getKey()) {
            groupInfo = CommonBeanUtils.copyProperties(existMsg.getValue(), InlongGroupInfo::new);
            this.groupContext.setGroupInfo(groupInfo);
            return init();
        } else {
            throw new RuntimeException(String.format("Group is not found by groupName=%s", groupInfo.getName()));
        }
    }

    @Override
    public InlongGroupContext suspend() throws Exception {
        return suspend(false);
    }

    @Override
    public InlongGroupContext suspend(boolean async) throws Exception {
        InlongGroupInfo groupInfo = groupContext.getGroupInfo();
        Pair<String, String> idAndErr = managerClient.updateGroup(groupInfo.genRequest());
        final String errMsg = idAndErr.getValue();
        final String groupId = idAndErr.getKey();
        AssertUtil.isNull(errMsg, errMsg);
        managerClient.operateInlongGroup(groupId, InlongGroupState.STOPPED, async);
        return generateSnapshot();
    }

    @Override
    public InlongGroupContext restart() throws Exception {
        return restart(false);
    }

    @Override
    public InlongGroupContext restart(boolean async) throws Exception {
        InlongGroupInfo groupInfo = groupContext.getGroupInfo();
        Pair<String, String> idAndErr = managerClient.updateGroup(groupInfo.genRequest());
        final String errMsg = idAndErr.getValue();
        final String groupId = idAndErr.getKey();
        AssertUtil.isNull(errMsg, errMsg);
        managerClient.operateInlongGroup(groupId, InlongGroupState.STARTED, async);
        return generateSnapshot();
    }

    @Override
    public InlongGroupContext delete() throws Exception {
        return delete(false);
    }

    @Override
    public InlongGroupContext delete(boolean async) throws Exception {
        InlongGroupResponse groupResponse = managerClient.getGroupInfo(
                groupContext.getGroupId());
        boolean isDeleted = managerClient.deleteInlongGroup(groupResponse.getInlongGroupId(), async);
        if (isDeleted) {
            groupResponse.setStatus(GroupStatus.DELETED.getCode());
        }
        return generateSnapshot();
    }

    @Override
    public List<InlongStream> listStreams() throws Exception {
        String inlongGroupId = this.groupContext.getGroupId();
        return fetchDataStreams(inlongGroupId);
    }

    private InlongGroupContext generateSnapshot() {
        //Fetch current group
        InlongGroupResponse groupResponse = managerClient.getGroupInfo(
                groupContext.getGroupId());
        InlongGroupInfo currentGroupInfo = CommonBeanUtils.copyProperties(groupResponse, InlongGroupInfo::new);
        groupContext.setGroupInfo(currentGroupInfo);
        String inlongGroupId = currentGroupInfo.getInlongGroupId();
        //Fetch stream in group
        List<InlongStream> dataStreams = fetchDataStreams(inlongGroupId);
        if (CollectionUtils.isNotEmpty(dataStreams)) {
            dataStreams.forEach(dataStream -> groupContext.setStream(dataStream));
        }
        //Create group context
        InlongGroupContext inlongGroupContext = new InlongGroupContext(groupContext, groupConf);
        //Fetch group logs
        List<EventLogView> logViews = managerClient.getInlongGroupError(inlongGroupId);
        if (CollectionUtils.isNotEmpty(logViews)) {
            Map<String, List<String>> errMsgs = Maps.newHashMap();
            Map<String, List<String>> groupLogs = Maps.newHashMap();
            logViews.stream()
                    .filter(x -> StringUtils.isNotEmpty(x.getElementName()))
                    .forEach(eventLogView -> {
                        String taskName = eventLogView.getElementName();
                        if (StringUtils.isNotEmpty(eventLogView.getException())) {
                            errMsgs.computeIfAbsent(taskName, Lists::newArrayList).add(eventLogView.getException());
                        }
                        if (StringUtils.isNotEmpty(eventLogView.getRemark())) {
                            groupLogs.computeIfAbsent(taskName, Lists::newArrayList).add(eventLogView.getRemark());
                        }
                    });
            inlongGroupContext.setGroupErrLogs(errMsgs);
            inlongGroupContext.setGroupLogs(groupLogs);
        }
        //Fetch stream logs
        Map<String, InlongStream> streams = inlongGroupContext.getInlongStreamMap();
        streams.keySet().stream().forEach(streamName -> {
            String inlongStreamId = "b_" + streamName;
            List<InlongStreamConfigLogListResponse> logList = managerClient.getStreamLogs(inlongGroupId,
                    inlongStreamId);
            if (CollectionUtils.isNotEmpty(logList)) {
                Map<String, List<String>> streamLogs = Maps.newHashMap();
                logList.stream().filter(x -> StringUtils.isNotEmpty(x.getComponentName()))
                        .forEach(streamLog -> {
                            String componentName = streamLog.getComponentName();
                            String log = GsonUtil.toJson(streamLog);
                            streamLogs.computeIfAbsent(componentName, Lists::newArrayList).add(log);
                        });
                inlongGroupContext.getStreamErrLogs().put(streamName, streamLogs);
            }
        });
        return inlongGroupContext;
    }

    private List<InlongStream> fetchDataStreams(String groupId) {
        List<FullStreamResponse> streamResponses = managerClient.listStreamInfo(groupId);
        if (CollectionUtils.isEmpty(streamResponses)) {
            return null;
        }
        return streamResponses.stream()
                .map(fullStreamResponse -> new InlongStreamImpl(fullStreamResponse, managerClient))
                .collect(Collectors.toList());
    }
}
