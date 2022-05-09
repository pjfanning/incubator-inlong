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

package org.apache.inlong.manager.client.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.pagehelper.PageInfo;
import org.apache.inlong.manager.client.api.InlongGroupContext.InlongGroupState;
import org.apache.inlong.manager.client.api.inner.InnerInlongManagerClient;
import org.apache.inlong.manager.client.cli.pojo.GroupInfo;
import org.apache.inlong.manager.client.cli.pojo.SinkInfo;
import org.apache.inlong.manager.client.cli.pojo.SourceInfo;
import org.apache.inlong.manager.client.cli.pojo.StreamInfo;
import org.apache.inlong.manager.client.cli.util.PrintUtil;
import org.apache.inlong.manager.common.pojo.group.InlongGroupListResponse;
import org.apache.inlong.manager.common.pojo.sink.SinkListResponse;
import org.apache.inlong.manager.common.pojo.source.SourceListResponse;
import org.apache.inlong.manager.common.pojo.stream.FullStreamResponse;
import org.apache.inlong.manager.common.pojo.stream.InlongStreamInfo;

import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "Displays main information for one or more resources")
public class CommandList extends CommandBase {

    @Parameter()
    private java.util.List<String> params;

    public CommandList() {
        super("list");
        jcommander.addCommand("stream", new CommandList.ListStream());
        jcommander.addCommand("group", new CommandList.ListGroup());
        jcommander.addCommand("sink", new CommandList.ListSink());
        jcommander.addCommand("source", new CommandList.ListSource());
    }

    @Parameters(commandDescription = "Get stream main information")
    private class ListStream extends CommandUtil {

        @Parameter()
        private java.util.List<String> params;

        @Parameter(names = {"-g", "--group"}, required = true, description = "inlong group id")
        private String groupId;

        @Override
        void run() {
            InnerInlongManagerClient managerClient = new InnerInlongManagerClient(connect().getConfiguration());
            try {
                List<FullStreamResponse> fullStreamResponseList = managerClient.listStreamInfo(groupId);
                List<InlongStreamInfo> inlongStreamInfoList = new ArrayList<>();
                fullStreamResponseList.forEach(fullStreamResponse -> {
                    inlongStreamInfoList.add(fullStreamResponse.getStreamInfo());
                });
                PrintUtil.print(inlongStreamInfoList, StreamInfo.class);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Parameters(commandDescription = "Get group details")
    private class ListGroup extends CommandUtil {

        @Parameter()
        private java.util.List<String> params;

        @Parameter(names = {"-s", "--status"})
        private String status;

        @Parameter(names = {"-g", "--group"}, description = "inlong group id")
        private String group;

        @Parameter(names = {"-n", "--num"}, description = "the number displayed")
        private int pageSize = 10;

        @Override
        void run() {
            try {
                InnerInlongManagerClient managerClient = new InnerInlongManagerClient(connect().getConfiguration());
                List<InlongGroupListResponse> groupList = new ArrayList<>();
                if (status != null) {
                    List<Integer> stateList = InlongGroupState.parseStatusByStrState(status);
                    for (int state : stateList) {
                        PageInfo<InlongGroupListResponse> groupPageInfo = managerClient.listGroups(group, state, 1,
                                pageSize);
                        groupList.addAll(groupPageInfo.getList());
                    }
                } else {
                    PageInfo<InlongGroupListResponse> groupPageInfo = managerClient.listGroups(group, 0, 1,
                            pageSize);
                    groupList = groupPageInfo.getList();
                }
                PrintUtil.print(groupList, GroupInfo.class);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Parameters(commandDescription = "Get sink details")
    private class ListSink extends CommandUtil {

        @Parameter()
        private java.util.List<String> params;

        @Parameter(names = {"-s", "--stream"}, required = true, description = "stream id")
        private String stream;

        @Parameter(names = {"-g", "--group"}, required = true, description = "group id")
        private String group;

        @Override
        void run() {
            InnerInlongManagerClient managerClient = new InnerInlongManagerClient(connect().getConfiguration());
            try {
                List<SinkListResponse> sinkListResponses = managerClient.listSinks(group, stream);
                PrintUtil.print(sinkListResponses, SinkInfo.class);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Parameters(commandDescription = "Get source details")
    private class ListSource extends CommandUtil {

        @Parameter()
        private java.util.List<String> params;

        @Parameter(names = {"-s", "--stream"}, required = true, description = "inlong stream id")
        private String stream;

        @Parameter(names = {"-g", "--group"}, required = true, description = "inlong group id")
        private String group;

        @Parameter(names = {"-t", "--type"}, description = "sink type")
        private String type;

        @Override
        void run() {
            InnerInlongManagerClient managerClient = new InnerInlongManagerClient(connect().getConfiguration());
            try {
                List<SourceListResponse> sourceListResponses = managerClient.listSources(group, stream, type);
                PrintUtil.print(sourceListResponses, SourceInfo.class);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
