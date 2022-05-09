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

package org.apache.inlong.manager.common.pojo.transform.filter;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.inlong.manager.common.enums.TransformType;
import org.apache.inlong.manager.common.pojo.stream.StreamField;
import org.apache.inlong.manager.common.pojo.transform.TransformDefinition;

import java.util.List;

/**
 * A class to define operation to filter stream records by different modes.
 * Rule mode is more recommended then script mode
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FilterDefinition extends TransformDefinition {

    public FilterDefinition(FilterStrategy filterStrategy, List<FilterRule> filterRules) {
        this.transformType = TransformType.FILTER;
        this.filterStrategy = filterStrategy;
        this.filterMode = FilterMode.RULE;
        this.filterRules = filterRules;
    }

    public FilterDefinition(FilterStrategy filterStrategy, ScriptBase scriptBase) {
        this.transformType = TransformType.FILTER;
        this.filterStrategy = filterStrategy;
        this.filterMode = FilterMode.SCRIPT;
        this.scriptBase = scriptBase;
    }

    @JsonFormat
    public enum FilterStrategy {
        RETAIN, REMOVE
    }

    @JsonFormat
    public enum FilterMode {
        RULE, SCRIPT
    }

    /**
     * Strategy for Filter transform
     */
    private FilterStrategy filterStrategy;

    /**
     * Mode for Filter transform
     */
    private FilterMode filterMode;

    @Data
    @AllArgsConstructor
    public static class TargetValue {

        /**
         * If target value is constant, set targetConstant, or set targetField if not;
         */
        private boolean isConstant;

        private StreamField targetField;

        private String targetConstant;
    }

    /**
     * Filter rule is about relationship between sourceField and targetValue;
     * such as 'a >= b' or 'a is not null'
     */
    @Data
    @AllArgsConstructor
    public static class FilterRule {

        private StreamField sourceField;

        private OperationType operationType;

        private TargetValue targetValue;

        private RuleRelation relationWithPost;
    }

    private List<FilterRule> filterRules;

    @Data
    @AllArgsConstructor
    public static class ScriptBase {

        private ScriptType scriptType;

        private String script;
    }

    private ScriptBase scriptBase;
}
