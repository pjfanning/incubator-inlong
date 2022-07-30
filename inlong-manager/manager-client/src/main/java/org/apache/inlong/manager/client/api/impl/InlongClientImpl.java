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

import com.github.pagehelper.PageInfo;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.inlong.manager.client.api.ClientConfiguration;
import org.apache.inlong.manager.client.api.InlongClient;
import org.apache.inlong.manager.client.api.InlongGroup;
import org.apache.inlong.manager.client.api.enums.SimpleGroupStatus;
import org.apache.inlong.manager.client.api.enums.SimpleSourceStatus;
import org.apache.inlong.manager.client.api.inner.client.ClientFactory;
import org.apache.inlong.manager.client.api.inner.client.InlongClusterClient;
import org.apache.inlong.manager.client.api.inner.client.InlongGroupClient;
import org.apache.inlong.manager.client.api.util.ClientUtils;
import org.apache.inlong.manager.common.pojo.cluster.BindTagRequest;
import org.apache.inlong.manager.common.pojo.cluster.ClusterInfo;
import org.apache.inlong.manager.common.pojo.cluster.ClusterNodeRequest;
import org.apache.inlong.manager.common.pojo.cluster.ClusterNodeResponse;
import org.apache.inlong.manager.common.pojo.cluster.ClusterPageRequest;
import org.apache.inlong.manager.common.pojo.cluster.ClusterRequest;
import org.apache.inlong.manager.common.pojo.cluster.ClusterTagPageRequest;
import org.apache.inlong.manager.common.pojo.cluster.ClusterTagRequest;
import org.apache.inlong.manager.common.pojo.cluster.ClusterTagResponse;
import org.apache.inlong.manager.common.pojo.group.InlongGroupBriefInfo;
import org.apache.inlong.manager.common.pojo.group.InlongGroupInfo;
import org.apache.inlong.manager.common.pojo.group.InlongGroupPageRequest;
import org.apache.inlong.manager.common.pojo.source.StreamSource;
import org.apache.inlong.manager.common.util.HttpUtils;
import org.apache.inlong.manager.common.util.Preconditions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Inlong client service implementation.
 */
@Slf4j
public class InlongClientImpl implements InlongClient {

    private static final String URL_SPLITTER = ",";
    private static final String HOST_SPLITTER = ":";
    @Getter
    private final ClientConfiguration configuration;
    private final InlongGroupClient groupClient;
    private final InlongClusterClient clusterClient;

    public InlongClientImpl(String serviceUrl, ClientConfiguration configuration) {
        Map<String, String> hostPorts = Splitter.on(URL_SPLITTER).withKeyValueSeparator(HOST_SPLITTER)
                .split(serviceUrl);
        if (MapUtils.isEmpty(hostPorts)) {
            throw new IllegalArgumentException(String.format("Unsupported serviceUrl: %s", serviceUrl));
        }
        configuration.setServiceUrl(serviceUrl);
        boolean isConnective = false;
        for (Map.Entry<String, String> hostPort : hostPorts.entrySet()) {
            String host = hostPort.getKey();
            int port = Integer.parseInt(hostPort.getValue());
            if (HttpUtils.checkConnectivity(host, port, configuration.getReadTimeout(), configuration.getTimeUnit())) {
                configuration.setBindHost(host);
                configuration.setBindPort(port);
                isConnective = true;
                break;
            }
        }
        if (!isConnective) {
            throw new RuntimeException(String.format("%s is not connective", serviceUrl));
        }
        this.configuration = configuration;
        ClientFactory clientFactory = ClientUtils.getClientFactory(configuration);
        groupClient = clientFactory.getGroupClient();
        clusterClient = clientFactory.getClusterClient();
    }

    @Override
    public InlongGroup forGroup(InlongGroupInfo groupInfo) {
        return new InlongGroupImpl(groupInfo, configuration);
    }

    @Override
    public List<InlongGroup> listGroup(String expr, int status, int pageNum, int pageSize) {
        PageInfo<InlongGroupBriefInfo> pageInfo = groupClient.listGroups(expr, status, pageNum,
                pageSize);
        if (CollectionUtils.isEmpty(pageInfo.getList())) {
            return Lists.newArrayList();
        } else {
            return pageInfo.getList().stream().map(briefInfo -> {
                String groupId = briefInfo.getInlongGroupId();
                InlongGroupInfo groupInfo = groupClient.getGroupInfo(groupId);
                return new InlongGroupImpl(groupInfo, configuration);
            }).collect(Collectors.toList());
        }
    }

    @Override
    public Map<String, SimpleGroupStatus> listGroupStatus(List<String> groupIds) {
        InlongGroupPageRequest request = new InlongGroupPageRequest();
        request.setGroupIdList(groupIds);
        request.setListSources(true);

        PageInfo<InlongGroupBriefInfo> pageInfo = groupClient.listGroups(request);
        List<InlongGroupBriefInfo> briefInfos = pageInfo.getList();
        Map<String, SimpleGroupStatus> groupStatusMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(briefInfos)) {
            briefInfos.forEach(briefInfo -> {
                String groupId = briefInfo.getInlongGroupId();
                SimpleGroupStatus groupStatus = SimpleGroupStatus.parseStatusByCode(briefInfo.getStatus());
                List<StreamSource> sources = briefInfo.getStreamSources();
                groupStatus = recheckGroupStatus(groupStatus, sources);
                groupStatusMap.put(groupId, groupStatus);
            });
        }
        return groupStatusMap;
    }

    @Override
    public InlongGroup getGroup(String groupId) {
        InlongGroupInfo groupInfo = groupClient.getGroupInfo(groupId);
        if (groupInfo == null) {
            return new BlankInlongGroup();
        }
        return new InlongGroupImpl(groupInfo, configuration);
    }

    @Override
    public Integer saveTag(ClusterTagRequest request) {
        Preconditions.checkNotNull(request, "inlong cluster request cannot be empty");
        Preconditions.checkNotNull(request.getClusterTag(), "cluster tag cannot be empty");
        return clusterClient.saveTag(request);
    }

    @Override
    public ClusterTagResponse getTag(Integer id) {
        Preconditions.checkNotNull(id, "inlong cluster tag id cannot be empty");
        return clusterClient.getTag(id);
    }

    @Override
    public PageInfo<ClusterTagResponse> listTag(ClusterTagPageRequest request) {
        return clusterClient.listTag(request);
    }

    @Override
    public Boolean updateTag(ClusterTagRequest request) {
        Preconditions.checkNotNull(request, "inlong cluster request cannot be empty");
        Preconditions.checkNotNull(request.getClusterTag(), "inlong cluster tag cannot be empty");
        Preconditions.checkNotNull(request.getId(), "cluster tag id cannot be empty");
        return clusterClient.updateTag(request);
    }

    @Override
    public Boolean deleteTag(Integer id) {
        Preconditions.checkNotNull(id, "cluster tag id cannot be empty");
        return clusterClient.deleteTag(id);
    }

    @Override
    public Integer saveCluster(ClusterRequest request) {
        Preconditions.checkNotNull(request, "inlong cluster request cannot be empty");
        return clusterClient.saveCluster(request);
    }

    @Override
    public ClusterInfo get(Integer id) {
        Preconditions.checkNotNull(id, "inlong cluster id cannot be empty");
        return clusterClient.get(id);
    }

    @Override
    public ClusterInfo list(ClusterPageRequest request) {
        return clusterClient.list(request);
    }

    @Override
    public Boolean update(ClusterRequest request) {
        Preconditions.checkNotNull(request, "inlong cluster info cannot be empty");
        Preconditions.checkNotNull(request.getId(), "inlong cluster id cannot be empty");
        return clusterClient.update(request);
    }

    @Override
    public Boolean bindTag(BindTagRequest request) {
        Preconditions.checkNotNull(request, "inlong cluster info cannot be empty");
        Preconditions.checkNotNull(request.getClusterTag(), "cluster tag cannot be empty");
        return clusterClient.bindTag(request);
    }

    @Override
    public Boolean delete(Integer id) {
        Preconditions.checkNotNull(id, "cluster id cannot be empty");
        return clusterClient.delete(id);
    }

    @Override
    public Integer saveNode(ClusterNodeRequest request) {
        Preconditions.checkNotNull(request, "cluster node info cannot be empty");
        return clusterClient.saveNode(request);
    }

    @Override
    public ClusterNodeResponse getNode(Integer id) {
        Preconditions.checkNotNull(id, "cluster node id cannot be empty");
        return clusterClient.getNode(id);
    }

    @Override
    public PageInfo<ClusterNodeResponse> listNode(ClusterPageRequest request) {
        Preconditions.checkNotNull(request.getParentId(), "Cluster id cannot be empty");
        return clusterClient.listNode(request);
    }

    @Override
    public Boolean updateNode(ClusterNodeRequest request) {
        Preconditions.checkNotNull(request, "inlong cluster node cannot be empty");
        Preconditions.checkNotNull(request.getId(), "cluster node id cannot be empty");
        return clusterClient.updateNode(request);
    }

    @Override
    public Boolean deleteNode(Integer id) {
        Preconditions.checkNotNull(id, "cluster node id cannot be empty");
        return clusterClient.deleteNode(id);
    }

    private SimpleGroupStatus recheckGroupStatus(SimpleGroupStatus groupStatus, List<StreamSource> sources) {
        Map<SimpleSourceStatus, List<StreamSource>> statusListMap = Maps.newHashMap();
        sources.forEach(source -> {
            SimpleSourceStatus status = SimpleSourceStatus.parseByStatus(source.getStatus());
            statusListMap.computeIfAbsent(status, k -> Lists.newArrayList()).add(source);
        });
        if (CollectionUtils.isNotEmpty(statusListMap.get(SimpleSourceStatus.FAILED))) {
            return SimpleGroupStatus.FAILED;
        }
        switch (groupStatus) {
            case STARTED:
                if (CollectionUtils.isNotEmpty(statusListMap.get(SimpleSourceStatus.INIT))) {
                    return SimpleGroupStatus.INITIALIZING;
                } else {
                    return groupStatus;
                }
            case STOPPED:
                if (CollectionUtils.isNotEmpty(statusListMap.get(SimpleSourceStatus.FREEZING))) {
                    return SimpleGroupStatus.OPERATING;
                } else {
                    return groupStatus;
                }
            case DELETED:
                if (CollectionUtils.isNotEmpty(statusListMap.get(SimpleSourceStatus.DELETING))) {
                    return SimpleGroupStatus.OPERATING;
                } else {
                    return groupStatus;
                }
            default:
                return groupStatus;
        }
    }
}
