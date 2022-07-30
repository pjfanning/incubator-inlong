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

package org.apache.inlong.agent.core.task;

import org.apache.inlong.agent.common.AbstractDaemon;
import org.apache.inlong.agent.common.AgentThreadFactory;
import org.apache.inlong.agent.conf.AgentConfiguration;
import org.apache.inlong.agent.constant.AgentConstants;
import org.apache.inlong.agent.core.AgentManager;
import org.apache.inlong.agent.utils.AgentUtils;
import org.apache.inlong.agent.utils.ConfigUtil;
import org.apache.inlong.agent.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Task manager maintains lots of tasks and communicate with job level components.
 * It also provide functions to execute commands from job level like killing/submit tasks.
 */
public class TaskManager extends AbstractDaemon {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManager.class);

    // task thread pool;
    private final ThreadPoolExecutor runningPool;
    private final AgentManager agentManager;
    private final TaskMetrics taskMetrics;
    private final ConcurrentHashMap<String, TaskWrapper> tasks;
    private final BlockingQueue<TaskWrapper> retryTasks;
    private final int monitorInterval;
    private final int taskMaxCapacity;
    private final int taskRetryMaxTime;
    private final long waitTime;

    /**
     * Init task manager.
     *
     * @param agentManager agent manager
     */
    public TaskManager(AgentManager agentManager) {
        this.agentManager = agentManager;
        this.runningPool = new ThreadPoolExecutor(
                0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new AgentThreadFactory("task"));
        // metric for task level
        if (ConfigUtil.isPrometheusEnabled()) {
            this.taskMetrics = new TaskPrometheusMetrics();
        } else {
            this.taskMetrics = TaskJmxMetrics.create();
        }

        tasks = new ConcurrentHashMap<>();
        AgentConfiguration conf = AgentConfiguration.getAgentConf();
        retryTasks = new LinkedBlockingQueue<>(
                conf.getInt(
                        AgentConstants.TASK_RETRY_MAX_CAPACITY, AgentConstants.DEFAULT_TASK_RETRY_MAX_CAPACITY));
        monitorInterval = conf.getInt(
                AgentConstants.TASK_MONITOR_INTERVAL, AgentConstants.DEFAULT_TASK_MONITOR_INTERVAL);
        taskRetryMaxTime = conf
                .getInt(AgentConstants.TASK_RETRY_SUBMIT_WAIT_SECONDS,
                        AgentConstants.DEFAULT_TASK_RETRY_SUBMIT_WAIT_SECONDS);
        taskMaxCapacity = conf.getInt(
                AgentConstants.TASK_RETRY_MAX_CAPACITY, AgentConstants.DEFAULT_TASK_RETRY_MAX_CAPACITY);
        waitTime = conf.getLong(
                AgentConstants.THREAD_POOL_AWAIT_TIME, AgentConstants.DEFAULT_THREAD_POOL_AWAIT_TIME);
    }

    /**
     * Get task metrics
     *
     * @return task metrics
     */
    public TaskMetrics getTaskMetrics() {
        return taskMetrics;
    }

    public TaskWrapper getTaskWrapper(String taskId) {
        return tasks.get(taskId);
    }

    /**
     * submit task, wait if task queue is full.
     *
     * @param task task
     */
    public void submitTask(Task task) {
        TaskWrapper taskWrapper = new TaskWrapper(agentManager, task);
        submitTask(taskWrapper);

    }

    public void submitTask(TaskWrapper wrapper) {
        TaskWrapper retTaskWrapper = tasks.putIfAbsent(wrapper.getTask().getTaskId(), wrapper);
        if (retTaskWrapper == null) {
            // pool may be full
            boolean notSubmitted = true;
            while (notSubmitted) {
                try {
                    this.runningPool.submit(wrapper);
                    notSubmitted = false;
                } catch (Exception ex) {
                    AgentUtils.silenceSleepInMs(waitTime);
                    LOGGER.warn("reject task {}", wrapper.getTask().getTaskId(), ex);
                }
            }
            taskMetrics.incRunningTaskCount();
        } else {
            LOGGER.warn("task cannot be repeated added taskId {}", wrapper.getTask().getTaskId());
        }
    }

    /**
     * retry task.
     *
     * @param wrapper task wrapper
     */
    private boolean addRetryTask(TaskWrapper wrapper) {
        LOGGER.info("retry submit task {}", wrapper.getTask().getTaskId());
        try {
            boolean success = retryTasks.offer(wrapper, taskRetryMaxTime, TimeUnit.SECONDS);
            if (!success) {
                LOGGER.error("cannot submit to retry queue, max {}, current {}", taskMaxCapacity,
                        retryTasks.size());
            } else {
                taskMetrics.incRetryingTaskCount();
            }
            return success;
        } catch (Exception ex) {
            LOGGER.error("error while offer task", ex);
        }
        return false;
    }

    /**
     * Check whether task is finished
     *
     * @param taskId task id
     * @return true if task is finished otherwise false
     */
    public boolean isTaskFinished(String taskId) {
        TaskWrapper wrapper = tasks.get(taskId);
        if (wrapper != null) {
            return wrapper.isFinished();
        }
        return false;
    }

    /**
     * Check if task is success
     *
     * @param taskId task id
     * @return true if task is success otherwise false
     */
    public boolean isTaskSuccess(String taskId) {
        TaskWrapper wrapper = tasks.get(taskId);
        if (wrapper != null) {
            return wrapper.isSuccess();
        }
        return false;
    }

    /**
     * Remove task and wait task to finish by task id
     *
     * @param taskId task id
     */
    public void removeTask(String taskId) {
        taskMetrics.decRunningTaskCount();
        TaskWrapper taskWrapper = tasks.remove(taskId);
        if (taskWrapper != null) {
            taskWrapper.destroyTask();
        }
    }

    /**
     * kill task
     *
     * @param task task
     */
    public boolean killTask(Task task) {
        // kill running tasks.
        TaskWrapper taskWrapper = tasks.get(task.getTaskId());
        if (taskWrapper != null) {
            taskWrapper.kill();
            return true;
        }
        return false;
    }

    /**
     * Thread for checking whether task should retry.
     *
     * @return runnable thread
     */
    public Runnable createTaskMonitorThread() {
        return () -> {
            while (isRunnable()) {
                try {
                    for (String taskId : tasks.keySet()) {
                        TaskWrapper wrapper = tasks.get(taskId);
                        if (wrapper != null && wrapper.isFailed() && wrapper.shouldRetry()) {
                            boolean success = addRetryTask(wrapper);
                            if (success) {
                                removeTask(taskId);
                            }
                        }
                    }
                    while (!retryTasks.isEmpty()) {
                        TaskWrapper taskWrapper = retryTasks.poll();
                        if (taskWrapper != null) {
                            taskMetrics.decRetryingTaskCount();
                            submitTask(taskWrapper);
                        }
                    }
                    TimeUnit.SECONDS.sleep(monitorInterval);
                } catch (Throwable ex) {
                    LOGGER.error("Exception caught", ex);
                    ThreadUtils.threadThrowableHandler(Thread.currentThread(), ex);
                }
            }
        };
    }

    /**
     * start service.
     */
    @Override
    public void start() {
        submitWorker(createTaskMonitorThread());
    }

    /**
     * stop service.
     */
    @Override
    public void stop() throws Exception {
        waitForTerminate();
        this.runningPool.shutdown();
    }
}
