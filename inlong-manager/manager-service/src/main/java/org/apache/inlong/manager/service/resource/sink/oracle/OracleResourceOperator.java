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

package org.apache.inlong.manager.service.resource.sink.oracle;

import org.apache.commons.collections.CollectionUtils;
import org.apache.inlong.manager.common.consts.InlongConstants;
import org.apache.inlong.manager.common.enums.SinkStatus;
import org.apache.inlong.manager.common.enums.SinkType;
import org.apache.inlong.manager.common.exceptions.WorkflowException;
import org.apache.inlong.manager.common.pojo.sink.SinkInfo;
import org.apache.inlong.manager.common.pojo.sink.oracle.OracleColumnInfo;
import org.apache.inlong.manager.common.pojo.sink.oracle.OracleSinkDTO;
import org.apache.inlong.manager.common.pojo.sink.oracle.OracleTableInfo;
import org.apache.inlong.manager.dao.entity.StreamSinkFieldEntity;
import org.apache.inlong.manager.dao.mapper.StreamSinkFieldEntityMapper;
import org.apache.inlong.manager.service.resource.sink.SinkResourceOperator;
import org.apache.inlong.manager.service.sink.StreamSinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class OracleResourceOperator implements SinkResourceOperator {


    private static final Logger LOG = LoggerFactory.getLogger(OracleResourceOperator.class);

    @Autowired
    private StreamSinkService sinkService;

    @Autowired
    private StreamSinkFieldEntityMapper fieldEntityMapper;

    @Override
    public Boolean accept(SinkType sinkType) {
        return SinkType.ORACLE == sinkType;
    }

    @Override
    public void createSinkResource(SinkInfo sinkInfo) {
        LOG.info("begin to create Oracle resources sinkId={}", sinkInfo.getId());
        if (SinkStatus.CONFIG_SUCCESSFUL.getCode().equals(sinkInfo.getStatus())) {
            LOG.warn("Oracle resource [" + sinkInfo.getId() + "] already success, skip to create");
            return;
        } else if (InlongConstants.DISABLE_CREATE_RESOURCE.equals(sinkInfo.getEnableCreateResource())) {
            LOG.warn("create resource was disabled, skip to create for [" + sinkInfo.getId() + "]");
            return;
        }
        this.createTable(sinkInfo);
    }

    /**
     * Create Oracle table by SinkInfo.
     *
     * @param sinkInfo {@link SinkInfo}
     */
    private void createTable(SinkInfo sinkInfo) {
        LOG.info("begin to create Oracle table for sinkId={}", sinkInfo.getId());
        List<StreamSinkFieldEntity> fieldList = fieldEntityMapper.selectBySinkId(sinkInfo.getId());
        if (CollectionUtils.isEmpty(fieldList)) {
            LOG.warn("no Oracle fields found, skip to create table for sinkId={}", sinkInfo.getId());
        }
        // set columns
        List<OracleColumnInfo> columnList = new ArrayList<>();
        for (StreamSinkFieldEntity field : fieldList) {
            OracleColumnInfo columnInfo = new OracleColumnInfo();
            columnInfo.setName(field.getFieldName());
            columnInfo.setType(field.getFieldType());
            columnInfo.setComment(field.getFieldComment());
            columnList.add(columnInfo);
        }

        OracleSinkDTO oracleSink = OracleSinkDTO.getFromJson(sinkInfo.getExtParams());
        OracleTableInfo tableInfo = OracleSinkDTO.getTableInfo(oracleSink, columnList);
        String url = oracleSink.getJdbcUrl();
        String user = oracleSink.getUsername();
        String password = oracleSink.getPassword();
        String tableName = tableInfo.getTableName();
        Connection conn = null;
        try {
            conn = OracleJdbcUtils.getConnection(url, user, password);

            // In Oracle, there is no need to consider whether the database exists
            // 1.If table not exists, create it
            OracleJdbcUtils.createTable(conn, tableInfo);
            // 2. table exists, add columns - skip the exists columns
            OracleJdbcUtils.addColumns(conn, tableName, columnList);
            // 3. update the sink status to success
            String info = "success to create Oracle resource";
            sinkService.updateStatus(sinkInfo.getId(), SinkStatus.CONFIG_SUCCESSFUL.getCode(), info);
            LOG.info(info + " for sinkInfo={}", sinkInfo);
            // 4. close connection.
            conn.close();
        } catch (Throwable e) {
            String errMsg = "create Oracle table failed: " + e.getMessage();
            LOG.error(errMsg, e);
            sinkService.updateStatus(sinkInfo.getId(), SinkStatus.CONFIG_FAILED.getCode(), errMsg);
            throw new WorkflowException(errMsg);
        } finally {
            try {
                if (null != conn) {
                    conn.close();
                    conn = null;
                }
            } catch (Throwable e) {
                String errMsg = "close Oracle connection failed: " + e.getMessage();
                throw new WorkflowException(errMsg);
            }
        }
        LOG.info("success create Oracle table for data sink [" + sinkInfo.getId() + "]");
    }
}
