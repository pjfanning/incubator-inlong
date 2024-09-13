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

package org.apache.inlong.sdk.transform.process.processor;

import org.apache.inlong.sdk.transform.decode.SourceDecoderFactory;
import org.apache.inlong.sdk.transform.encode.SinkEncoderFactory;
import org.apache.inlong.sdk.transform.pojo.FieldInfo;
import org.apache.inlong.sdk.transform.pojo.KvSinkInfo;
import org.apache.inlong.sdk.transform.pojo.KvSourceInfo;
import org.apache.inlong.sdk.transform.pojo.TransformConfig;
import org.apache.inlong.sdk.transform.process.TransformProcessor;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestKv2StarProcessor extends AbstractProcessorTestBase {

    @Test
    public void testKv2Star() throws Exception {
        List<FieldInfo> fields = this.getTestFieldList("ftime", "extinfo");
        KvSourceInfo kvSource = new KvSourceInfo("UTF-8", fields);
        KvSinkInfo kvSink = new KvSinkInfo("UTF-8", new ArrayList<>());
        String transformSql = "select *";
        TransformConfig config = new TransformConfig(transformSql);
        // case1
        TransformProcessor<String, String> processor1 = TransformProcessor
                .create(config, SourceDecoderFactory.createKvDecoder(kvSource),
                        SinkEncoderFactory.createKvEncoder(kvSink));
        List<String> output1 = processor1.transform("ftime=2024-04-28 00:00:00&extinfo=ok", new HashMap<>());
        Assert.assertEquals(1, output1.size());
        Assert.assertEquals(output1.get(0), "ftime=2024-04-28 00:00:00&extinfo=ok");
        // case2
        config.setTransformSql("select * from source where extinfo!='ok'");
        TransformProcessor<String, String> processor2 = TransformProcessor
                .create(config, SourceDecoderFactory.createKvDecoder(kvSource),
                        SinkEncoderFactory.createKvEncoder(kvSink));
        List<String> output2 = processor2.transform("ftime=2024-04-28 00:00:00&extinfo=ok", new HashMap<>());
        Assert.assertEquals(0, output2.size());
        // case3
        config.setTransformSql("select *,extinfo e1,ftime f1 from source where extinfo!='ok'");
        TransformProcessor<String, String> processor3 = TransformProcessor
                .create(config, SourceDecoderFactory.createKvDecoder(kvSource),
                        SinkEncoderFactory.createKvEncoder(kvSink));

        List<String> output3 = processor3.transform("ftime=2024-04-28 00:00:00&extinfo=nok", new HashMap<>());
        Assert.assertEquals(1, output3.size());
        Assert.assertEquals(output3.get(0), "ftime=2024-04-28 00:00:00&extinfo=nok&e1=nok&f1=2024-04-28 00:00:00");
    }
}