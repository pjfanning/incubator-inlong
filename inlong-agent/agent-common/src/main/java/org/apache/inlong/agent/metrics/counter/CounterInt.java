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

package org.apache.inlong.agent.metrics.counter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * atomic int for counter
 */
public class CounterInt implements Counter {

    private AtomicInteger value = new AtomicInteger(0);

    @Override
    public void incr() {
        value.incrementAndGet();
    }

    @Override
    public void incr(int delta) {
        assert delta > 0;
        value.getAndAdd(delta);
    }

    @Override
    public Integer snapshot() {
        return value.get();
    }
}
