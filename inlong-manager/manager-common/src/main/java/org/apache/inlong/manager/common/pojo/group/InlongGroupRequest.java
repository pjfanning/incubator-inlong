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

package org.apache.inlong.manager.common.pojo.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Inlong group request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("Inlong group create request")
public class InlongGroupRequest {

    @ApiModelProperty(value = "Inlong group id", required = true)
    private String inlongGroupId;

    @ApiModelProperty(value = "Inlong group name", required = true)
    private String name;

    @ApiModelProperty(value = "Chinese display name")
    private String cnName;

    @ApiModelProperty(value = "Inlong group description")
    private String description;

    @NotNull(message = "middlewareType cannot be null")
    @ApiModelProperty(value = "Middleware type, high throughput: TUBE, high consistency: PULSAR")
    private String middlewareType;

    @ApiModelProperty(value = "Queue model of Pulsar, parallel: multiple partitions, high throughput, out-of-order "
            + "messages; serial: single partition, low throughput, and orderly messages")
    @Builder.Default
    private String queueModule = "parallel";

    @ApiModelProperty(value = "The number of partitions of Pulsar Topic, 1-20")
    @Builder.Default
    private Integer topicPartitionNum = 3;

    @ApiModelProperty(value = "MQ resource object, in inlong group",
            notes = "Tube corresponds to Topic, Pulsar corresponds to Namespace")
    private String mqResourceObj;

    @ApiModelProperty(value = "Tube master URL")
    private String tubeMaster;

    @ApiModelProperty(value = "Pulsar admin URL")
    private String pulsarAdminUrl;

    @ApiModelProperty(value = "Pulsar service URL")
    private String pulsarServiceUrl;

    @ApiModelProperty(value = "Whether zookeeper enabled? 0: disabled, 1: enabled")
    @Builder.Default
    private Integer zookeeperEnabled = 0;

    @ApiModelProperty(value = "Data type name")
    private String schemaName;

    @ApiModelProperty(value = "Number of access items per day, unit: 10,000 items per day")
    private Integer dailyRecords;

    @ApiModelProperty(value = "Access size per day, unit: GB per day")
    private Integer dailyStorage;

    @ApiModelProperty(value = "peak access per second, unit: bars per second")
    private Integer peakRecords;

    @ApiModelProperty(value = "The maximum length of a single piece of data, unit: Byte")
    private Integer maxLength;

    @ApiModelProperty(value = "Name of responsible person, separated by commas")
    private String inCharges;

    @ApiModelProperty(value = "Name of followers, separated by commas")
    private String followers;

    @ApiModelProperty(value = "Name of creator")
    private String creator;

    @ApiModelProperty(value = "Temporary view, string in JSON format")
    private String tempView;

    @ApiModelProperty(value = "data proxy cluster id")
    private Integer proxyClusterId;

    @ApiModelProperty(value = "Inlong group Extension properties")
    private List<InlongGroupExtInfo> extList;

    @ApiModelProperty(value = "The extension info for MQ")
    private InlongGroupMqExtBase mqExtInfo;

}
