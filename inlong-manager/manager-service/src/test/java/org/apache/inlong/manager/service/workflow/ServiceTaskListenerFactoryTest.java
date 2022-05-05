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

package org.apache.inlong.manager.service.workflow;

import org.apache.inlong.manager.common.enums.MQType;
import org.apache.inlong.manager.common.pojo.group.InlongGroupInfo;
import org.apache.inlong.manager.common.pojo.group.InlongGroupPulsarInfo;
import org.apache.inlong.manager.common.pojo.workflow.form.GroupResourceProcessForm;
import org.apache.inlong.manager.service.ServiceBaseTest;
import org.apache.inlong.manager.service.mq.CreatePulsarGroupTaskListener;
import org.apache.inlong.manager.service.mq.CreatePulsarResourceTaskListener;
import org.apache.inlong.manager.service.mq.CreateTubeGroupTaskListener;
import org.apache.inlong.manager.service.mq.CreateTubeTopicTaskListener;
import org.apache.inlong.manager.workflow.WorkflowContext;
import org.apache.inlong.manager.workflow.event.task.QueueOperateListener;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ServiceTaskListenerFactoryTest extends ServiceBaseTest {

    @Autowired
    ServiceTaskListenerFactory serviceTaskListenerFactory;

    @Test
    public void testGetQueueOperateListener() {
        GroupResourceProcessForm processForm = new GroupResourceProcessForm();
        InlongGroupInfo groupInfo = new InlongGroupInfo();
        //check pulsar listener
        groupInfo.setMiddlewareType(MQType.PULSAR.getType());
        groupInfo.setMqExtInfo(new InlongGroupPulsarInfo());
        processForm.setGroupInfo(groupInfo);
        WorkflowContext context = new WorkflowContext();
        context.setProcessForm(processForm);
        List<QueueOperateListener> queueOperateListeners = serviceTaskListenerFactory.getQueueOperateListener(context);
        if (queueOperateListeners.size() == 0) {
            return;
        }
        Assert.assertEquals(2, queueOperateListeners.size());
        Assert.assertTrue(queueOperateListeners.get(0) instanceof CreatePulsarResourceTaskListener);
        Assert.assertTrue(queueOperateListeners.get(1) instanceof CreatePulsarGroupTaskListener);

        // check tube listener
        groupInfo.setMiddlewareType(MQType.TUBE.getType());
        queueOperateListeners = serviceTaskListenerFactory.getQueueOperateListener(context);
        Assert.assertEquals(2, queueOperateListeners.size());
        Assert.assertTrue(queueOperateListeners.get(0) instanceof CreateTubeTopicTaskListener);
        Assert.assertTrue(queueOperateListeners.get(1) instanceof CreateTubeGroupTaskListener);
    }

}
