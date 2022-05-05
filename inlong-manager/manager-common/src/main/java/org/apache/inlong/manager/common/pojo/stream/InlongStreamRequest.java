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

package org.apache.inlong.manager.common.pojo.stream;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Inlong stream request.
 */
@Data
@ApiModel("Inlong stream request")
public class InlongStreamRequest {

    @ApiModelProperty(value = "Primary key")
    private Integer id;

    @ApiModelProperty(value = "Inlong group id")
    private String inlongGroupId;

    @ApiModelProperty(value = "Inlong stream id")
    private String inlongStreamId;

    @ApiModelProperty(value = "Inlong stream name", required = true)
    private String name;

    @ApiModelProperty(value = "Inlong stream description")
    private String description;

    @ApiModelProperty(value = "MQ resource object, in inlong group",
            notes = "Tube corresponds to Topic, Pulsar corresponds to Namespace")
    private String mqResourceObj;

    @ApiModelProperty(value = "Data storage period, unit: day (required when dataSourceType=AUTO_PUSH)")
    private Integer storagePeriod;

    @ApiModelProperty(value = "Data type, including: TEXT, KV, etc.")
    private String dataType;

    @ApiModelProperty(value = "Data encoding format: UTF-8, GBK")
    private String dataEncoding;

    @ApiModelProperty(value = "Data separator, stored as ASCII code")
    private String dataSeparator;

    @ApiModelProperty(value = "Data field escape symbol, stored as ASCII code")
    private String dataEscapeChar;

    @ApiModelProperty(value = "(File and DB access) Whether there are predefined fields, 0: no, 1: yes")
    private Integer havePredefinedFields;

    @ApiModelProperty(value = "Whether to send synchronously, 0: no, 1: yes",
            notes = "Each task under this stream sends data synchronously, "
                    + "which will affect the throughput of data collection, please choose carefully")
    private Integer syncSend;

    @ApiModelProperty(value = "Number of access items per day, unit: 10,000 items per day")
    private Integer dailyRecords;

    @ApiModelProperty(value = "Access size per day, unit: GB per day")
    private Integer dailyStorage;

    @ApiModelProperty(value = "peak access per second, unit: bars per second")
    private Integer peakRecords;

    @ApiModelProperty(value = "The maximum length of a single piece of data, unit: Byte")
    private Integer maxLength;

    @ApiModelProperty(value = "Names of responsible persons, separated by commas")
    private String inCharges;

    @ApiModelProperty(value = "StreamPipeline snapshot of stream,, string in JSON format")
    private String tempView;

    @ApiModelProperty(value = "Field list")
    private List<InlongStreamFieldInfo> fieldList;

}
