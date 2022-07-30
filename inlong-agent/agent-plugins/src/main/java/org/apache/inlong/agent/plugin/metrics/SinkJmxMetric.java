/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.inlong.agent.plugin.metrics;

import org.apache.inlong.agent.metrics.Metric;
import org.apache.inlong.common.metric.Dimension;
import org.apache.inlong.common.metric.MetricDomain;
import org.apache.inlong.common.metric.MetricItem;
import org.apache.inlong.common.metric.MetricRegister;

import java.util.concurrent.atomic.AtomicLong;

@MetricDomain(name = "SinkMetric")
public class SinkJmxMetric extends MetricItem implements SinkMetric {

    @Dimension
    private final String tagName;

    @Metric
    private final AtomicLong sourceSuccessCounter = new AtomicLong(0);

    @Metric
    private final AtomicLong sourceFailCounter = new AtomicLong(0);

    public SinkJmxMetric(String tagName) {
        this.tagName = tagName;
        MetricRegister.register(this);
    }

    @Override
    public String getTagName() {
        return tagName;
    }

    @Override
    public void incSinkSuccessCount() {
        sourceSuccessCounter.incrementAndGet();
    }

    @Override
    public long getSinkSuccessCount() {
        return sourceSuccessCounter.get();
    }

    @Override
    public void incSinkFailCount() {
        sourceFailCounter.incrementAndGet();
    }

    @Override
    public long getSinkFailCount() {
        return sourceFailCounter.get();
    }
}
