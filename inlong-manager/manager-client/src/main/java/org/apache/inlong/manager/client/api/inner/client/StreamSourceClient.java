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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.inlong.manager.client.api.ClientConfiguration;
import org.apache.inlong.manager.client.api.service.StreamSourceApi;
import org.apache.inlong.manager.client.api.util.ClientUtils;
import org.apache.inlong.manager.common.beans.Response;
import org.apache.inlong.manager.common.pojo.source.SourceRequest;
import org.apache.inlong.manager.common.pojo.source.StreamSource;
import org.apache.inlong.manager.common.util.Preconditions;

import java.util.List;

/**
 * Client for {@link StreamSourceApi}.
 */
public class StreamSourceClient {

    private final StreamSourceApi streamSourceApi;

    public StreamSourceClient(ClientConfiguration configuration) {
        streamSourceApi = ClientUtils.createRetrofit(configuration).create(StreamSourceApi.class);
    }

    /**
     * Create an inlong stream source.
     */
    public Integer createSource(SourceRequest request) {
        Response<Integer> response = ClientUtils.executeHttpCall(streamSourceApi.createSource(request));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }

    /**
     * List stream sources by the given groupId and streamId.
     */
    public List<StreamSource> listSources(String groupId, String streamId) {
        return listSources(groupId, streamId, null);
    }

    /**
     * List stream sources by the specified source type.
     */
    public List<StreamSource> listSources(String groupId, String streamId, String sourceType) {
        Response<PageInfo<StreamSource>> response = ClientUtils.executeHttpCall(
                streamSourceApi.listSources(groupId, streamId, sourceType));
        ClientUtils.assertRespSuccess(response);
        return response.getData().getList();
    }

    /**
     * Update the stream source info.
     */
    public Pair<Boolean, String> updateSource(SourceRequest request) {
        Response<Boolean> response = ClientUtils.executeHttpCall(streamSourceApi.updateSource(request));
        if (response.getData() != null) {
            return Pair.of(response.getData(), response.getErrMsg());
        } else {
            return Pair.of(false, response.getErrMsg());
        }
    }

    /**
     * Delete data source information by id.
     */
    public boolean deleteSource(int id) {
        Preconditions.checkTrue(id > 0, "sourceId is illegal");
        Response<Boolean> response = ClientUtils.executeHttpCall(streamSourceApi.deleteSource(id));
        ClientUtils.assertRespSuccess(response);
        return response.getData();
    }
}
