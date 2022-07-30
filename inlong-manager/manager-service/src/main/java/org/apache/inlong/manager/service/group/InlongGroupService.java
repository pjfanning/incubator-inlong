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

package org.apache.inlong.manager.service.group;

import com.github.pagehelper.PageInfo;
import org.apache.inlong.manager.common.pojo.group.InlongGroupApproveRequest;
import org.apache.inlong.manager.common.pojo.group.InlongGroupBriefInfo;
import org.apache.inlong.manager.common.pojo.group.InlongGroupCountResponse;
import org.apache.inlong.manager.common.pojo.group.InlongGroupExtInfo;
import org.apache.inlong.manager.common.pojo.group.InlongGroupInfo;
import org.apache.inlong.manager.common.pojo.group.InlongGroupPageRequest;
import org.apache.inlong.manager.common.pojo.group.InlongGroupRequest;
import org.apache.inlong.manager.common.pojo.group.InlongGroupTopicInfo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Inlong group service layer interface
 */
public interface InlongGroupService {

    /**
     * Save inlong group info.
     *
     * @param groupInfo group request need to save
     * @param operator name of operator
     * @return inlong group id after saving
     */
    String save(InlongGroupRequest groupInfo, String operator);

    /**
     * Get inlong group info based on inlong group id
     *
     * @param groupId inlong group id
     * @return detail of inlong group
     */
    InlongGroupInfo get(String groupId);

    /**
     * Paging query inlong group brief info list
     *
     * @param request pagination query request
     * @return group list
     */
    PageInfo<InlongGroupBriefInfo> listBrief(InlongGroupPageRequest request);

    /**
     * Modify group information
     *
     * @param request inlong group request that needs to be modified
     * @param operator name of operator
     * @return inlong group id
     */
    String update(@Valid @NotNull(message = "inlong group request cannot be null") InlongGroupRequest request,
            String operator);

    /**
     * Modify the status of the specified group
     *
     * @param groupId inlong group id
     * @param status modified status
     * @param operator name of operator
     * @return whether succeed
     */
    boolean updateStatus(String groupId, Integer status, String operator);

    /**
     * Delete the group information of the specified group id
     *
     * @param groupId The group id that needs to be deleted
     * @param operator name of operator
     * @return whether succeed
     */
    boolean delete(String groupId, String operator);

    /**
     * Query whether the specified group id exists
     *
     * @param groupId the group id to be queried
     * @return does it exist
     */
    Boolean exist(String groupId);

    /**
     * Query the group information of each status of the current user
     *
     * @param operator name of operator
     * @return inlong group status statistics
     */
    InlongGroupCountResponse countGroupByUser(String operator);

    /**
     * According to the group id, query the topic to which it belongs
     *
     * @param groupId Inlong group id
     * @return Topic information
     * @apiNote Tube corresponds to the group, only 1 topic
     */
    InlongGroupTopicInfo getTopic(String groupId);

    /**
     * Save the group modified when the approval is passed
     *
     * @param approveRequest approval information
     * @param operator name of operator
     */
    void updateAfterApprove(
            @Valid @NotNull(message = "approve request cannot be null") InlongGroupApproveRequest approveRequest,
            String operator);

    /**
     * Save or update extended information
     * <p/>First physically delete the existing extended information, and then add this batch of extended information
     *
     * @param groupId inlong group id
     * @param infoList inlong group ext info list
     */
    void saveOrUpdateExt(String groupId, List<InlongGroupExtInfo> infoList);

}
