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

package org.apache.inlong.manager.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.inlong.manager.common.pojo.sortstandalone.SortIdInfo;
import org.apache.inlong.manager.common.pojo.sortstandalone.SortSourceStreamInfo;
import org.apache.inlong.manager.common.pojo.sortstandalone.SortTaskInfo;
import org.apache.inlong.manager.common.pojo.sink.SinkBriefInfo;
import org.apache.inlong.manager.common.pojo.sink.SinkInfo;
import org.apache.inlong.manager.common.pojo.sink.SinkPageRequest;
import org.apache.inlong.manager.dao.entity.StreamSinkEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreamSinkEntityMapper {

    int insert(StreamSinkEntity record);

    int insertSelective(StreamSinkEntity record);

    StreamSinkEntity selectByPrimaryKey(Integer id);

    /**
     * According to the inlong group id and inlong stream id, query the number of valid sink
     *
     * @param groupId inlong group id
     * @param streamId inlong stream id
     * @return Sink entity size
     */
    int selectCount(@Param("groupId") String groupId, @Param("streamId") String streamId);

    /**
     * Paging query sink list based on conditions
     *
     * @param request Paging query conditions
     * @return Sink entity list
     */
    List<StreamSinkEntity> selectByCondition(@Param("request") SinkPageRequest request);

    /**
     * Query the sink summary from the given groupId and streamId
     */
    List<SinkBriefInfo> selectSummary(@Param("groupId") String groupId,
            @Param("streamId") String streamId);

    /**
     * Query valid sink list by the given group id and stream id.
     *
     * @param groupId Inlong group id.
     * @param streamId Inlong stream id.
     * @param sinkName Stream sink name.
     * @return Sink entity list.
     */
    List<StreamSinkEntity> selectByRelatedId(@Param("groupId") String groupId, @Param("streamId") String streamId,
            @Param("sinkName") String sinkName);

    /**
     * According to the group id, stream id and sink type, query valid sink entity list.
     *
     * @param groupId Inlong group id.
     * @param streamId Inlong stream id.
     * @param sinkType Sink type.
     * @return Sink entity list.
     */
    List<StreamSinkEntity> selectByIdAndType(@Param("groupId") String groupId, @Param("streamId") String streamId,
            @Param("sinkType") String sinkType);

    /**
     * Filter stream ids with the specified groupId and sinkType from the given stream id list.
     *
     * @param groupId Inlong group id.
     * @param sinkType Sink type.
     * @param streamIdList Inlong stream id list.
     * @return List of Inlong stream id with the given sink type
     */
    List<String> selectExistsStreamId(@Param("groupId") String groupId, @Param("sinkType") String sinkType,
            @Param("streamIdList") List<String> streamIdList);

    /**
     * Get the distinct sink type from the given groupId and streamId
     */
    List<String> selectSinkType(@Param("groupId") String groupId, @Param("streamId") String streamId);

    /**
     * Select all config for Sort under the group id and stream id
     *
     * @param groupId inlong group id
     * @param streamIdList list of the inlong stream id, if is null, get all infos under the group id
     * @return Sort config
     */
    List<SinkInfo> selectAllConfig(@Param("groupId") String groupId, @Param("idList") List<String> streamIdList);

    int updateByPrimaryKeySelective(StreamSinkEntity record);

    int updateByPrimaryKey(StreamSinkEntity record);

    int updateStatus(StreamSinkEntity entity);

    int deleteByPrimaryKey(Integer id);

    /**
     * Select all tasks for sort-standalone
     *
     * @return All tasks
     */
    List<SortTaskInfo> selectAllTasks();

    /**
     * Select all id params for sort-standalone
     *
     * @return All id params
     */
    List<SortIdInfo> selectAllIdParams();

    /**
     * Select all streams for sort sdk.
     *
     * @return All stream info
     */
    List<SortSourceStreamInfo> selectAllStreams();

}