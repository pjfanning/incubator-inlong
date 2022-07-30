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

package org.apache.inlong.manager.service;

import org.apache.inlong.manager.common.consts.InlongConstants;
import org.apache.inlong.manager.common.enums.FieldType;
import org.apache.inlong.manager.common.enums.GroupStatus;
import org.apache.inlong.manager.common.enums.MQType;
import org.apache.inlong.manager.common.pojo.group.InlongGroupInfo;
import org.apache.inlong.manager.common.pojo.group.none.InlongNoneMqInfo;
import org.apache.inlong.manager.common.pojo.group.pulsar.InlongPulsarInfo;
import org.apache.inlong.manager.common.pojo.stream.InlongStreamInfo;
import org.apache.inlong.manager.common.pojo.stream.InlongStreamRequest;
import org.apache.inlong.manager.common.pojo.stream.StreamField;
import org.apache.inlong.manager.service.core.InlongStreamService;
import org.apache.inlong.manager.service.group.InlongGroupService;
import org.apache.inlong.manager.test.BaseTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for base test service.
 */
@SpringBootApplication
@SpringBootTest(classes = ServiceBaseTest.class)
public class ServiceBaseTest extends BaseTest {

    public static final String GLOBAL_GROUP_ID = "global_group";
    public static final String GLOBAL_STREAM_ID = "global_stream";
    public static final String GLOBAL_OPERATOR = "admin";
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBaseTest.class);

    @Autowired
    protected InlongGroupService groupService;
    @Autowired
    protected InlongStreamService streamService;

    @Test
    public void test() {
        LOGGER.info("The test class cannot be empty, otherwise 'No runnable methods exception' will be reported");
    }

    /**
     * Create InlongGroup from the given specified InlongGroupId
     *
     * @return InlongGroupInfo after saving
     */
    public InlongGroupInfo createInlongGroup(String inlongGroupId, String mqType) {
        try {
            streamService.logicDeleteAll(inlongGroupId, GLOBAL_OPERATOR);
            groupService.delete(inlongGroupId, GLOBAL_OPERATOR);
        } catch (Exception e) {
            // ignore
        }

        InlongGroupInfo groupInfo;
        if (MQType.forType(mqType) == MQType.PULSAR || MQType.forType(mqType) == MQType.TDMQ_PULSAR) {
            groupInfo = new InlongPulsarInfo();
        } else if (MQType.forType(mqType) == MQType.TUBE) {
            groupInfo = new InlongPulsarInfo();
        } else {
            groupInfo = new InlongNoneMqInfo();
        }

        groupInfo.setInlongGroupId(inlongGroupId);
        groupInfo.setMqType(mqType);
        groupInfo.setMqResource("test-queue");
        groupInfo.setInCharges(GLOBAL_OPERATOR);
        groupInfo.setEnableCreateResource(InlongConstants.ENABLE_CREATE_RESOURCE);
        groupService.save(groupInfo.genRequest(), GLOBAL_OPERATOR);
        InlongGroupInfo updateGroupInfo = groupService.get(inlongGroupId);
        groupService.updateStatus(inlongGroupId, GroupStatus.TO_BE_APPROVAL.getCode(), GLOBAL_OPERATOR);
        groupService.updateStatus(inlongGroupId, GroupStatus.APPROVE_PASSED.getCode(), GLOBAL_OPERATOR);
        groupService.update(updateGroupInfo.genRequest(), GLOBAL_OPERATOR);

        return groupInfo;
    }

    /**
     * Create InlongStream from the given InlongGroupInfo and specified InlongStreamId
     *
     * @return InlongStreamInfo after saving
     */
    public InlongStreamInfo createStreamInfo(InlongGroupInfo groupInfo, String inlongStreamId) {
        String inlongGroupId = groupInfo.getInlongGroupId();
        // delete first
        try {
            streamService.delete(inlongGroupId, inlongStreamId, GLOBAL_OPERATOR);
        } catch (Exception e) {
            // ignore
        }

        InlongStreamRequest request = new InlongStreamRequest();
        request.setInlongGroupId(inlongGroupId);
        request.setInlongStreamId(inlongStreamId);
        request.setMqResource(inlongStreamId);
        request.setDataSeparator("124");
        request.setDataEncoding("UTF-8");
        request.setFieldList(createStreamFields(inlongGroupId, inlongStreamId));
        streamService.save(request, GLOBAL_OPERATOR);

        return streamService.get(request.getInlongGroupId(), request.getInlongStreamId());
    }

    /**
     * Get StreamField list from the given groupId and streamId
     *
     * @return list of StreamField
     */
    public List<StreamField> createStreamFields(String groupId, String streamId) {
        final List<StreamField> streamFields = new ArrayList<>();
        StreamField fieldInfo = new StreamField();
        fieldInfo.setInlongGroupId(groupId);
        fieldInfo.setInlongStreamId(streamId);
        fieldInfo.setFieldName("id");
        fieldInfo.setFieldType(FieldType.INT.toString());
        fieldInfo.setFieldComment("idx");
        streamFields.add(fieldInfo);
        return streamFields;
    }

}
