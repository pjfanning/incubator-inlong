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

package org.apache.inlong.manager.common.pojo.source.kafka;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.inlong.manager.common.enums.SourceType;
import org.apache.inlong.manager.common.pojo.source.SourceResponse;

/**
 * Response of the kafka source
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "Response of the kafka source")
public class KafkaSourceResponse extends SourceResponse {

    @ApiModelProperty("Kafka topic")
    private String topic;

    @ApiModelProperty("Kafka consumer group")
    private String groupId;

    @ApiModelProperty("Kafka servers address")
    private String bootstrapServers;

    @ApiModelProperty("Limit the amount of data read per second")
    private String recordSpeedLimit;

    @ApiModelProperty("Limit the number of bytes read per second")
    private String byteSpeedLimit;

    @ApiModelProperty("Topic partition offset")
    private String topicPartitionOffset;

    @ApiModelProperty(value = "The strategy of auto offset reset")
    private String autoOffsetReset;

    @ApiModelProperty("database pattern used for filter in canal format")
    private String databasePattern;

    @ApiModelProperty("table pattern used for filter in canal format")
    private String tablePattern;

    @ApiModelProperty("ignore parse errors, true: ignore parse error; false: not ignore parse error; default true")
    private boolean ignoreParseErrors;

    @ApiModelProperty("Timestamp standard for binlog: SQL, ISO_8601")
    private String timestampFormatStandard;

    @ApiModelProperty("Primary key, needed when serialization type is csv, json, avro")
    private String primaryKey;

    public KafkaSourceResponse() {
        this.setSourceType(SourceType.KAFKA.name());
    }

}
