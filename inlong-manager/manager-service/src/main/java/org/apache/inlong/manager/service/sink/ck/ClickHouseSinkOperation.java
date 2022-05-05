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

package org.apache.inlong.manager.service.sink.ck;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.inlong.manager.common.enums.ErrorCodeEnum;
import org.apache.inlong.manager.common.enums.GlobalConstants;
import org.apache.inlong.manager.common.enums.SinkStatus;
import org.apache.inlong.manager.common.enums.SinkType;
import org.apache.inlong.manager.common.exceptions.BusinessException;
import org.apache.inlong.manager.common.pojo.sink.SinkFieldRequest;
import org.apache.inlong.manager.common.pojo.sink.SinkFieldResponse;
import org.apache.inlong.manager.common.pojo.sink.SinkListResponse;
import org.apache.inlong.manager.common.pojo.sink.SinkRequest;
import org.apache.inlong.manager.common.pojo.sink.SinkResponse;
import org.apache.inlong.manager.common.pojo.sink.ck.ClickHouseSinkDTO;
import org.apache.inlong.manager.common.pojo.sink.ck.ClickHouseSinkListResponse;
import org.apache.inlong.manager.common.pojo.sink.ck.ClickHouseSinkRequest;
import org.apache.inlong.manager.common.pojo.sink.ck.ClickHouseSinkResponse;
import org.apache.inlong.manager.common.util.CommonBeanUtils;
import org.apache.inlong.manager.common.util.Preconditions;
import org.apache.inlong.manager.dao.entity.StreamSinkEntity;
import org.apache.inlong.manager.dao.entity.StreamSinkFieldEntity;
import org.apache.inlong.manager.dao.mapper.StreamSinkEntityMapper;
import org.apache.inlong.manager.dao.mapper.StreamSinkFieldEntityMapper;
import org.apache.inlong.manager.service.sink.StreamSinkOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

/**
 * ClickHouse sink operation
 */
@Service
public class ClickHouseSinkOperation implements StreamSinkOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClickHouseSinkOperation.class);

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private StreamSinkEntityMapper sinkMapper;
    @Autowired
    private StreamSinkFieldEntityMapper sinkFieldMapper;

    @Override
    public Boolean accept(SinkType sinkType) {
        return SinkType.CLICKHOUSE.equals(sinkType);
    }

    @Override
    public Integer saveOpt(SinkRequest request, String operator) {
        String sinkType = request.getSinkType();
        Preconditions.checkTrue(SinkType.SINK_CLICKHOUSE.equals(sinkType),
                ErrorCodeEnum.SINK_TYPE_NOT_SUPPORT.getMessage() + ": " + sinkType);

        ClickHouseSinkRequest sinkRequest = (ClickHouseSinkRequest) request;
        StreamSinkEntity entity = CommonBeanUtils.copyProperties(sinkRequest, StreamSinkEntity::new);
        entity.setStatus(SinkStatus.NEW.getCode());
        entity.setIsDeleted(GlobalConstants.UN_DELETED);
        entity.setCreator(operator);
        entity.setModifier(operator);
        Date now = new Date();
        entity.setCreateTime(now);
        entity.setModifyTime(now);

        // get the ext params
        ClickHouseSinkDTO dto = ClickHouseSinkDTO.getFromRequest(sinkRequest);
        try {
            entity.setExtParams(objectMapper.writeValueAsString(dto));
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.SINK_SAVE_FAILED);
        }
        sinkMapper.insert(entity);

        Integer sinkId = entity.getId();
        request.setId(sinkId);
        this.saveFieldOpt(request);

        return sinkId;
    }

    @Override
    public void saveFieldOpt(SinkRequest request) {
        List<SinkFieldRequest> fieldList = request.getFieldList();
        LOGGER.info("begin to save field={}", fieldList);
        if (CollectionUtils.isEmpty(fieldList)) {
            return;
        }

        int size = fieldList.size();
        List<StreamSinkFieldEntity> entityList = new ArrayList<>(size);
        String groupId = request.getInlongGroupId();
        String streamId = request.getInlongStreamId();
        String sinkType = request.getSinkType();
        Integer sinkId = request.getId();
        for (SinkFieldRequest fieldInfo : fieldList) {
            StreamSinkFieldEntity fieldEntity = CommonBeanUtils.copyProperties(fieldInfo, StreamSinkFieldEntity::new);
            if (StringUtils.isEmpty(fieldEntity.getFieldComment())) {
                fieldEntity.setFieldComment(fieldEntity.getFieldName());
            }
            fieldEntity.setInlongGroupId(groupId);
            fieldEntity.setInlongStreamId(streamId);
            fieldEntity.setSinkType(sinkType);
            fieldEntity.setSinkId(sinkId);
            fieldEntity.setIsDeleted(GlobalConstants.UN_DELETED);
            entityList.add(fieldEntity);
        }

        sinkFieldMapper.insertAll(entityList);
        LOGGER.info("success to save clickHouse field");
    }

    @Override
    public SinkResponse getById(@NotNull String sinkType, @NotNull Integer id) {
        StreamSinkEntity entity = sinkMapper.selectByPrimaryKey(id);
        Preconditions.checkNotNull(entity, ErrorCodeEnum.SINK_INFO_NOT_FOUND.getMessage());
        String existType = entity.getSinkType();
        Preconditions.checkTrue(SinkType.SINK_CLICKHOUSE.equals(existType),
                String.format(ErrorCodeEnum.SINK_TYPE_NOT_SAME.getMessage(), SinkType.SINK_CLICKHOUSE, existType));

        SinkResponse response = this.getFromEntity(entity, ClickHouseSinkResponse::new);
        List<StreamSinkFieldEntity> entities = sinkFieldMapper.selectBySinkId(id);
        List<SinkFieldResponse> infos = CommonBeanUtils.copyListProperties(entities,
                SinkFieldResponse::new);
        response.setFieldList(infos);

        return response;
    }

    @Override
    public <T> T getFromEntity(StreamSinkEntity entity, Supplier<T> target) {
        T result = target.get();
        if (entity == null) {
            return result;
        }

        String existType = entity.getSinkType();
        Preconditions.checkTrue(SinkType.SINK_CLICKHOUSE.equals(existType),
                String.format(ErrorCodeEnum.SINK_TYPE_NOT_SAME.getMessage(), SinkType.SINK_CLICKHOUSE, existType));

        ClickHouseSinkDTO dto = ClickHouseSinkDTO.getFromJson(entity.getExtParams());
        CommonBeanUtils.copyProperties(entity, result, true);
        CommonBeanUtils.copyProperties(dto, result, true);

        return result;
    }

    @Override
    public PageInfo<? extends SinkListResponse> getPageInfo(Page<StreamSinkEntity> entityPage) {
        if (CollectionUtils.isEmpty(entityPage)) {
            return new PageInfo<>();
        }
        return entityPage.toPageInfo(entity -> this.getFromEntity(entity, ClickHouseSinkListResponse::new));
    }

    @Override
    public void updateOpt(SinkRequest request, String operator) {
        String sinkType = request.getSinkType();
        Preconditions.checkTrue(SinkType.SINK_CLICKHOUSE.equals(sinkType),
                String.format(ErrorCodeEnum.SINK_TYPE_NOT_SAME.getMessage(), SinkType.SINK_CLICKHOUSE, sinkType));

        StreamSinkEntity entity = sinkMapper.selectByPrimaryKey(request.getId());
        Preconditions.checkNotNull(entity, ErrorCodeEnum.SINK_INFO_NOT_FOUND.getMessage());
        ClickHouseSinkRequest sinkRequest = (ClickHouseSinkRequest) request;
        CommonBeanUtils.copyProperties(sinkRequest, entity, true);
        try {
            ClickHouseSinkDTO dto = ClickHouseSinkDTO.getFromRequest(sinkRequest);
            entity.setExtParams(objectMapper.writeValueAsString(dto));
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.SINK_INFO_INCORRECT.getMessage());
        }

        entity.setPreviousStatus(entity.getStatus());
        entity.setStatus(SinkStatus.CONFIG_ING.getCode());
        entity.setModifier(operator);
        entity.setModifyTime(new Date());
        sinkMapper.updateByPrimaryKeySelective(entity);

        boolean onlyAdd = SinkStatus.CONFIG_SUCCESSFUL.getCode().equals(entity.getPreviousStatus());
        this.updateFieldOpt(onlyAdd, sinkRequest);

        LOGGER.info("success to update sink of type={}", sinkType);
    }

    @Override
    public void updateFieldOpt(Boolean onlyAdd, SinkRequest request) {
        Integer sinkId = request.getId();
        List<SinkFieldRequest> fieldRequestList = request.getFieldList();
        if (CollectionUtils.isEmpty(fieldRequestList)) {
            return;
        }

        if (onlyAdd) {
            List<StreamSinkFieldEntity> existsFieldList = sinkFieldMapper.selectBySinkId(sinkId);
            if (existsFieldList.size() > fieldRequestList.size()) {
                throw new BusinessException(ErrorCodeEnum.SINK_FIELD_UPDATE_NOT_ALLOWED);
            }
            for (int i = 0; i < existsFieldList.size(); i++) {
                if (!existsFieldList.get(i).getFieldName().equals(fieldRequestList.get(i).getFieldName())) {
                    throw new BusinessException(ErrorCodeEnum.SINK_FIELD_UPDATE_NOT_ALLOWED);
                }
            }
        }

        // First physically delete the existing fields
        sinkFieldMapper.deleteAll(sinkId);
        // Then batch save the sink fields
        this.saveFieldOpt(request);

        LOGGER.info("success to update field");
    }

}
