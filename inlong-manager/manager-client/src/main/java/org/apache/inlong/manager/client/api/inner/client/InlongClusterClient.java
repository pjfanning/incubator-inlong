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

package org.apache.inlong.manager.client.api.inner.client;

import com.github.pagehelper.PageInfo;
import org.apache.inlong.manager.client.api.ClientConfiguration;
import org.apache.inlong.manager.client.api.service.InlongClusterApi;
import org.apache.inlong.manager.client.api.util.ClientUtils;
import org.apache.inlong.manager.common.beans.Response;
import org.apache.inlong.manager.common.pojo.cluster.BindTagRequest;
import org.apache.inlong.manager.common.pojo.cluster.ClusterInfo;
import org.apache.inlong.manager.common.pojo.cluster.ClusterNodeRequest;
import org.apache.inlong.manager.common.pojo.cluster.ClusterNodeResponse;
import org.apache.inlong.manager.common.pojo.cluster.ClusterPageRequest;
import org.apache.inlong.manager.common.pojo.cluster.ClusterRequest;
import org.apache.inlong.manager.common.pojo.cluster.ClusterTagPageRequest;
import org.apache.inlong.manager.common.pojo.cluster.ClusterTagRequest;
import org.apache.inlong.manager.common.pojo.cluster.ClusterTagResponse;
import org.apache.inlong.manager.common.util.Preconditions;

/**
 * Client for {@link InlongClusterApi}.
 */
public class InlongClusterClient {

    private final InlongClusterApi inlongClusterApi;

    public InlongClusterClient(ClientConfiguration configuration) {
        inlongClusterApi = ClientUtils.createRetrofit(configuration).create(InlongClusterApi.class);
    }

    /**
     * Save cluster tag.
     *
     * @param request cluster tag
     * @return saved cluster tag id
     */
    public Integer saveTag(ClusterTagRequest request) {
        Response<Integer> response = ClientUtils.executeHttpCall(inlongClusterApi.saveTag(request));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }

    /**
     * Get cluster tag by id.
     *
     * @param id cluster tag id
     * @return cluster tag info
     */
    public ClusterTagResponse getTag(Integer id) {
        Preconditions.checkNotNull(id, "cluster id should not be empty");
        Response<ClusterTagResponse> response = ClientUtils.executeHttpCall(inlongClusterApi.getTag(id));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }

    /**
     * Paging query cluster tags according to conditions.
     *
     * @param request page request conditions
     * @return cluster tag list
     */
    public PageInfo<ClusterTagResponse> listTag(ClusterTagPageRequest request) {
        Response<PageInfo<ClusterTagResponse>> response = ClientUtils.executeHttpCall(
                inlongClusterApi.listTag(request));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }

    /**
     * Update cluster tag.
     *
     * @param request cluster tag to be modified
     * @return whether succeed
     */
    public Boolean updateTag(ClusterTagRequest request) {
        Response<Boolean> response = ClientUtils.executeHttpCall(inlongClusterApi.updateTag(request));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }

    /**
     * Delete cluster tag.
     *
     * @param id cluster tag id to be deleted
     * @return whether succeed
     */
    public Boolean deleteTag(Integer id) {
        Preconditions.checkNotNull(id, "cluster id should not be empty");
        Response<Boolean> response = ClientUtils.executeHttpCall(inlongClusterApi.deleteTag(id));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }

    /**
     * Save component cluster for Inlong.
     *
     * @param request cluster create request
     * @return clusterIndex
     */
    public Integer saveCluster(ClusterRequest request) {
        Preconditions.checkNotEmpty(request.getName(), "cluster name should not be empty");
        Preconditions.checkNotEmpty(request.getType(), "cluster type should not be empty");
        Preconditions.checkNotEmpty(request.getClusterTags(), "cluster tags should not be empty");
        Response<Integer> clusterIndexResponse = ClientUtils.executeHttpCall(inlongClusterApi.save(request));
        ClientUtils.assertRespSuccess(clusterIndexResponse);
        return clusterIndexResponse.getData();
    }

    /**
     * Get cluster info by id.
     *
     * @param id cluster id
     * @return cluster info
     */
    public ClusterInfo get(Integer id) {
        Preconditions.checkNotNull(id, "cluster id should not be empty");
        Response<ClusterInfo> clusterInfoResponse = ClientUtils.executeHttpCall(inlongClusterApi.get(id));
        ClientUtils.assertRespSuccess(clusterInfoResponse);
        return clusterInfoResponse.getData();
    }

    /**
     * Paging query clusters according to conditions.
     *
     * @param request query conditions
     * @return cluster list
     */
    public ClusterInfo list(ClusterPageRequest request) {
        Response<ClusterInfo> clusterInfoResponse = ClientUtils.executeHttpCall(inlongClusterApi.list(request));
        ClientUtils.assertRespSuccess(clusterInfoResponse);
        return clusterInfoResponse.getData();
    }

    /**
     * Update cluster information.
     *
     * @param request cluster to be modified
     * @return whether succeed
     */
    public Boolean update(ClusterRequest request) {
        Response<Boolean> response = ClientUtils.executeHttpCall(inlongClusterApi.update(request));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }

    /**
     * Bind or unbind cluster tag for clusters.
     *
     * @param request cluster to be modified
     * @return whether succeed
     */
    public Boolean bindTag(BindTagRequest request) {
        Response<Boolean> response = ClientUtils.executeHttpCall(inlongClusterApi.bindTag(request));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }

    /**
     * Delete cluster information.
     *
     * @param id cluster id to be deleted
     * @return whether succeed
     */
    public Boolean delete(Integer id) {
        Preconditions.checkNotNull(id, "cluster id should not be empty");
        Response<Boolean> response = ClientUtils.executeHttpCall(inlongClusterApi.delete(id));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }

    /**
     * Save cluster node info.
     *
     * @param request cluster info
     * @return id after saving
     */
    public Integer saveNode(ClusterNodeRequest request) {
        Response<Integer> response = ClientUtils.executeHttpCall(inlongClusterApi.saveNode(request));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }

    /**
     * Get cluster node info by id.
     *
     * @param id cluster id
     * @return cluster info
     */
    public ClusterNodeResponse getNode(Integer id) {
        Preconditions.checkNotNull(id, "cluster id should not be empty");
        Response<ClusterNodeResponse> response = ClientUtils.executeHttpCall(inlongClusterApi.getNode(id));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }

    /**
     * Paging query cluster nodes according to conditions.
     *
     * @param request page request conditions
     * @return cluster node list
     */
    public PageInfo<ClusterNodeResponse> listNode(ClusterPageRequest request) {
        Response<PageInfo<ClusterNodeResponse>> response = ClientUtils.executeHttpCall(
                inlongClusterApi.listNode(request));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }

    /**
     * Update cluster node.
     *
     * @param request cluster node to be modified
     * @return whether succeed
     */
    public Boolean updateNode(ClusterNodeRequest request) {
        Response<Boolean> response = ClientUtils.executeHttpCall(inlongClusterApi.updateNode(request));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }

    /**
     * Delete cluster node.
     *
     * @param id cluster node id to be deleted
     * @return whether succeed
     */
    public Boolean deleteNode(Integer id) {
        Preconditions.checkNotNull(id, "cluster id should not be empty");
        Response<Boolean> response = ClientUtils.executeHttpCall(inlongClusterApi.deleteNode(id));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }
}
