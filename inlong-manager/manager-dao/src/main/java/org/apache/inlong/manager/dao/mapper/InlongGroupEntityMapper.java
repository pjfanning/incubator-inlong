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
import org.apache.inlong.manager.common.pojo.group.InlongGroupBriefInfo;
import org.apache.inlong.manager.common.pojo.group.InlongGroupPageRequest;
import org.apache.inlong.manager.common.pojo.sortstandalone.SortSourceGroupInfo;
import org.apache.inlong.manager.dao.entity.InlongGroupEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface InlongGroupEntityMapper {

    int insert(InlongGroupEntity record);

    InlongGroupEntity selectByPrimaryKey(Integer id);

    List<Map<String, Object>> countGroupByUser(@Param(value = "username") String username);

    InlongGroupEntity selectByGroupId(String groupId);

    InlongGroupEntity selectByGroupIdForUpdate(String groupId);

    List<InlongGroupEntity> selectByCondition(InlongGroupPageRequest request);

    List<InlongGroupBriefInfo> selectBriefList(InlongGroupPageRequest request);

    List<InlongGroupEntity> selectByClusterTag(@Param(value = "inlongClusterTag") String inlongClusterTag);

    int updateByPrimaryKey(InlongGroupEntity record);

    int updateByIdentifierSelective(InlongGroupEntity record);

    int updateStatus(@Param("groupId") String groupId, @Param("status") Integer status,
            @Param("modifier") String modifier);

    int deleteByPrimaryKey(Integer id);

    /**
     * Select all group info for sort sdk.
     *
     * @return All inlong group info.
     */
    List<SortSourceGroupInfo> selectAllGroups();

}