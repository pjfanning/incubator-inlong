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

package org.apache.inlong.manager.service.core.sink;

import org.apache.inlong.manager.common.enums.GlobalConstants;
import org.apache.inlong.manager.common.enums.SinkType;
import org.apache.inlong.manager.common.pojo.sink.SinkResponse;
import org.apache.inlong.manager.common.pojo.sink.iceberg.IcebergSinkRequest;
import org.apache.inlong.manager.common.pojo.sink.iceberg.IcebergSinkResponse;
import org.apache.inlong.manager.common.util.CommonBeanUtils;
import org.apache.inlong.manager.service.ServiceBaseTest;
import org.apache.inlong.manager.service.core.impl.InlongStreamServiceTest;
import org.apache.inlong.manager.service.sink.StreamSinkService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class IcebergStreamSinkServiceTest extends ServiceBaseTest {

    private final String globalGroupId = "b_group1";
    private final String globalStreamId = "stream1_iceberg";
    private final String globalOperator = "admin";
    // private final String sinkName = "default";

    @Autowired
    private StreamSinkService sinkService;
    @Autowired
    private InlongStreamServiceTest streamServiceTest;

    public Integer saveSink(String sinkName) {
        streamServiceTest.saveInlongStream(globalGroupId, globalStreamId, globalOperator);
        IcebergSinkRequest sinkInfo = new IcebergSinkRequest();
        sinkInfo.setInlongGroupId(globalGroupId);
        sinkInfo.setInlongStreamId(globalStreamId);
        sinkInfo.setSinkType(SinkType.SINK_ICEBERG);
        sinkInfo.setEnableCreateResource(GlobalConstants.DISABLE_CREATE_RESOURCE);
        sinkInfo.setDataPath("hdfs://127.0.0.1:8020/data");
        sinkInfo.setSinkName(sinkName);
        sinkInfo.setId((int) (Math.random() * 100000 + 1));
        return sinkService.save(sinkInfo, globalOperator);
    }

    @Test
    public void testSaveAndDelete() {
        Integer id = this.saveSink("default1");
        Assert.assertNotNull(id);
        boolean result = sinkService.delete(id, SinkType.SINK_ICEBERG, globalOperator);
        Assert.assertTrue(result);
    }

    @Test
    public void testListByIdentifier() {
        Integer id = this.saveSink("default2");
        SinkResponse sink = sinkService.get(id, SinkType.SINK_ICEBERG);
        Assert.assertEquals(globalGroupId, sink.getInlongGroupId());
        sinkService.delete(id, SinkType.SINK_ICEBERG, globalOperator);
    }

    @Test
    public void testGetAndUpdate() {
        Integer id = this.saveSink("default3");
        SinkResponse response = sinkService.get(id, SinkType.SINK_ICEBERG);
        Assert.assertEquals(globalGroupId, response.getInlongGroupId());

        IcebergSinkResponse icebergSinkResponse = (IcebergSinkResponse) response;
        icebergSinkResponse.setEnableCreateResource(GlobalConstants.DISABLE_CREATE_RESOURCE);

        IcebergSinkRequest request = CommonBeanUtils.copyProperties(icebergSinkResponse,
                IcebergSinkRequest::new);
        boolean result = sinkService.update(request, globalOperator);
        Assert.assertTrue(result);
    }
}
