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

package org.apache.inlong.agent.plugin.sources;

import org.apache.inlong.agent.conf.JobProfile;
import org.apache.inlong.agent.plugin.Reader;
import org.apache.inlong.agent.plugin.Source;
import org.apache.inlong.agent.plugin.metrics.GlobalMetrics;
import org.apache.inlong.agent.plugin.sources.reader.SqlReader;
import org.apache.inlong.agent.utils.AgentDbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.inlong.agent.constant.CommonConstants.DEFAULT_PROXY_INLONG_GROUP_ID;
import static org.apache.inlong.agent.constant.CommonConstants.DEFAULT_PROXY_INLONG_STREAM_ID;
import static org.apache.inlong.agent.constant.CommonConstants.PROXY_INLONG_GROUP_ID;
import static org.apache.inlong.agent.constant.CommonConstants.PROXY_INLONG_STREAM_ID;

/**
 * Make database as Source
 */
public class DatabaseSqlSource implements Source {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSqlSource.class);

    private static final String JOB_DATABASE_SQL = "job.sql.command";

    private static final String DATABASE_SOURCE_TAG_NAME = "AgentDatabaseSourceMetric";

    private static AtomicLong metricsIndex = new AtomicLong(0);

    public DatabaseSqlSource() {
    }

    /**
     * Use SQL to read data.
     *
     * @param sqlPattern sql pattern
     * @return list of readers or null if sql is not correct.
     */
    private List<Reader> splitSqlJob(String sqlPattern) {
        String[] sqlList = AgentDbUtils.replaceDynamicSeq(sqlPattern);
        if (sqlList != null) {
            List<Reader> result = new ArrayList<>();
            for (String sql : sqlList) {
                result.add(new SqlReader(sql));
            }
            return result;
        }
        return null;
    }

    /**
     * Use SQL or binlog to read data.
     *
     * @return reader list or null if database type is not correct.
     */
    @Override
    public List<Reader> split(JobProfile conf) {
        String inlongGroupId = conf.get(PROXY_INLONG_GROUP_ID, DEFAULT_PROXY_INLONG_GROUP_ID);
        String inlongStreamId = conf.get(PROXY_INLONG_STREAM_ID, DEFAULT_PROXY_INLONG_STREAM_ID);
        String metricTagName = DATABASE_SOURCE_TAG_NAME + "_" + inlongGroupId + "_" + inlongStreamId;
        String sqlPattern = conf.get(JOB_DATABASE_SQL, "").toLowerCase();
        List<Reader> readerList = null;
        if (!sqlPattern.isEmpty()) {
            readerList = splitSqlJob(sqlPattern);
        }
        if (readerList != null) {
            // increment the count of successful sources
            GlobalMetrics.incSourceSuccessCount(metricTagName);
        } else {
            // database type or sql is incorrect
            // increment the count of failed sources
            GlobalMetrics.incSourceFailCount(metricTagName);
        }
        return readerList;
    }
}
