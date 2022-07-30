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

package org.apache.inlong.manager.client.api.service;

import com.github.pagehelper.PageInfo;
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
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface InlongClusterApi {

    @POST("cluster/tag/save")
    Call<Response<Integer>> saveTag(@Body ClusterTagRequest request);

    @GET("cluster/tag/get/{id}")
    Call<Response<ClusterTagResponse>> getTag(@Path("id") Integer id);

    @POST("cluster/tag/list")
    Call<Response<PageInfo<ClusterTagResponse>>> listTag(@Body ClusterTagPageRequest request);

    @POST("cluster/tag/update")
    Call<Response<Boolean>> updateTag(@Body ClusterTagRequest request);

    @DELETE("cluster/tag/delete/{id}")
    Call<Response<Boolean>> deleteTag(@Path("id") Integer id);

    @POST("cluster/save")
    Call<Response<Integer>> save(@Body ClusterRequest request);

    @GET("cluster/get/{id}")
    Call<Response<ClusterInfo>> get(@Path("id") Integer id);

    @POST("cluster/list")
    Call<Response<ClusterInfo>> list(@Body ClusterPageRequest request);

    @POST("cluster/update")
    Call<Response<Boolean>> update(@Body ClusterRequest request);

    @POST("cluster/bindTag")
    Call<Response<Boolean>> bindTag(@Body BindTagRequest request);

    @DELETE("cluster/delete/{id}")
    Call<Response<Boolean>> delete(@Path("id") Integer id);

    @POST("cluster/node/save")
    Call<Response<Integer>> saveNode(@Body ClusterNodeRequest request);

    @GET("cluster/node/get/{id}")
    Call<Response<ClusterNodeResponse>> getNode(@Path("id") Integer id);

    @POST("cluster/node/list")
    Call<Response<PageInfo<ClusterNodeResponse>>> listNode(@Body ClusterPageRequest request);

    @POST("cluster/node/update")
    Call<Response<Boolean>> updateNode(@Body ClusterNodeRequest request);

    @DELETE("cluster/node/delete/{id}")
    Call<Response<Boolean>> deleteNode(@Path("id") Integer id);
}
