/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.dataproxy.metrics.audit;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flume.Event;
import org.apache.inlong.audit.AuditImp;
import org.apache.inlong.audit.util.AuditConfig;
import org.apache.inlong.dataproxy.config.holder.CommonPropertiesHolder;
import org.apache.inlong.dataproxy.consts.AttributeConstants;
import org.apache.inlong.dataproxy.metrics.DataProxyMetricItem;
import org.apache.inlong.dataproxy.utils.Constants;

import java.util.HashSet;
import java.util.Map;

/**
 * 
 * AuditUtils
 */
public class AuditUtils {

    public static final String AUDIT_KEY_FILE_PATH = "audit.filePath";
    public static final String AUDIT_DEFAULT_FILE_PATH = "/data/inlong/audit/";
    public static final String AUDIT_KEY_MAX_CACHE_ROWS = "audit.maxCacheRows";
    public static final int AUDIT_DEFAULT_MAX_CACHE_ROWS = 2000000;
    public static final String AUDIT_KEY_PROXYS = "audit.proxys";
    public static final String AUDIT_KEY_IS_AUDIT = "audit.enable";

    public static final int AUDIT_ID_DATAPROXY_READ_SUCCESS = 5;
    public static final int AUDIT_ID_DATAPROXY_SEND_SUCCESS = 6;

    private static boolean IS_AUDIT = true;

    /**
     * initAudit
     */
    public static void initAudit() {
        // IS_AUDIT
        IS_AUDIT = BooleanUtils.toBoolean(CommonPropertiesHolder.getString(AUDIT_KEY_IS_AUDIT));
        if (IS_AUDIT) {
            // AuditProxy
            String strIpPorts = CommonPropertiesHolder.getString(AUDIT_KEY_PROXYS);
            HashSet<String> proxys = new HashSet<>();
            if (!StringUtils.isBlank(strIpPorts)) {
                String[] ipPorts = strIpPorts.split("\\s+");
                for (String ipPort : ipPorts) {
                    proxys.add(ipPort);
                }
            }
            AuditImp.getInstance().setAuditProxy(proxys);
            // AuditConfig
            String filePath = CommonPropertiesHolder.getString(AUDIT_KEY_FILE_PATH, AUDIT_DEFAULT_FILE_PATH);
            int maxCacheRow = NumberUtils.toInt(
                    CommonPropertiesHolder.getString(AUDIT_KEY_MAX_CACHE_ROWS),
                    AUDIT_DEFAULT_MAX_CACHE_ROWS);
            AuditConfig auditConfig = new AuditConfig(filePath, maxCacheRow);
            AuditImp.getInstance().setAuditConfig(auditConfig);
        }
    }

    /**
     * add
     * 
     * @param auditID
     * @param event
     */
    public static void add(int auditID, Event event) {
        if (IS_AUDIT && event != null) {
            Map<String, String> headers = event.getHeaders();
            String inlongGroupId = DataProxyMetricItem.getInlongGroupId(headers);
            String inlongStreamId = DataProxyMetricItem.getInlongStreamId(headers);
            long logTime = getLogTime(headers);
            AuditImp.getInstance().add(auditID, inlongGroupId, inlongStreamId, logTime, 1, event.getBody().length);
        }
    }

    /**
     * getLogTime
     * 
     * @param  headers
     * @return
     */
    public static long getLogTime(Map<String, String> headers) {
        String strLogTime = headers.get(Constants.HEADER_KEY_MSG_TIME);
        if (strLogTime == null) {
            strLogTime = headers.get(AttributeConstants.DATA_TIME);
        }
        if (strLogTime == null) {
            return System.currentTimeMillis();
        }
        long logTime = NumberUtils.toLong(strLogTime, 0);
        if (logTime == 0) {
            logTime = System.currentTimeMillis();
        }
        return logTime;
    }

    /**
     * getLogTime
     * 
     * @param  event
     * @return
     */
    public static long getLogTime(Event event) {
        if (event != null) {
            Map<String, String> headers = event.getHeaders();
            return getLogTime(headers);
        }
        return System.currentTimeMillis();
    }

    /**
     * getAuditFormatTime
     * 
     * @param  msgTime
     * @return
     */
    public static long getAuditFormatTime(long msgTime) {
        long auditFormatTime = msgTime - msgTime % CommonPropertiesHolder.getAuditFormatInterval();
        return auditFormatTime;
    }

    /**
     * sendReport
     */
    public static void sendReport() {
        AuditImp.getInstance().sendReport();
    }
}
