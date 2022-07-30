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
import org.apache.inlong.manager.common.pojo.sink.SinkRequest;
import org.apache.inlong.manager.common.pojo.sink.StreamSink;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StreamSinkApi {

    @POST("sink/save")
    Call<Response<Integer>> createSink(@Body SinkRequest request);

    @POST("sink/update")
    Call<Response<Boolean>> updateSink(@Body SinkRequest request);

    @DELETE("sink/delete/{id}")
    Call<Response<Boolean>> deleteSink(@Path("id") Integer id);

    @GET("sink/list")
    Call<Response<PageInfo<StreamSink>>> listSinks(@Query("inlongGroupId") String groupId,
            @Query("inlongStreamId") String streamId, @Query("sinkType") String sinkType);

    @GET("sink/get/{id}")
    Call<Response<StreamSink>> getSinkInfo(@Path("id") Integer sinkId);

}
