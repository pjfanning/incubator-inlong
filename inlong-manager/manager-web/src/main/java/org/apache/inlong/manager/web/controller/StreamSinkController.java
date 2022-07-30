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

package org.apache.inlong.manager.web.controller;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.inlong.manager.common.beans.Response;
import org.apache.inlong.manager.common.enums.OperationType;
import org.apache.inlong.manager.common.pojo.common.UpdateValidation;
import org.apache.inlong.manager.common.pojo.sink.SinkPageRequest;
import org.apache.inlong.manager.common.pojo.sink.SinkRequest;
import org.apache.inlong.manager.common.pojo.sink.StreamSink;
import org.apache.inlong.manager.common.util.LoginUserUtils;
import org.apache.inlong.manager.service.core.operationlog.OperationLog;
import org.apache.inlong.manager.service.sink.StreamSinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stream sink control layer
 */
@RestController
@RequestMapping("/api")
@Api(tags = "Stream-Sink-API")
public class StreamSinkController {

    @Autowired
    private StreamSinkService sinkService;

    @RequestMapping(value = "/sink/save", method = RequestMethod.POST)
    @OperationLog(operation = OperationType.CREATE)
    @ApiOperation(value = "Save stream sink")
    public Response<Integer> save(@Validated @RequestBody SinkRequest request) {
        return Response.success(sinkService.save(request, LoginUserUtils.getLoginUser().getName()));
    }

    @RequestMapping(value = "/sink/get/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "Get stream sink")
    @ApiImplicitParam(name = "id", dataTypeClass = Integer.class, required = true)
    public Response<StreamSink> get(@PathVariable Integer id) {
        return Response.success(sinkService.get(id));
    }

    @RequestMapping(value = "/sink/list", method = RequestMethod.GET)
    @ApiOperation(value = "Get stream sink list by paginating")
    public Response<PageInfo<? extends StreamSink>> listByCondition(SinkPageRequest request) {
        return Response.success(sinkService.listByCondition(request));
    }

    @RequestMapping(value = "/sink/update", method = RequestMethod.POST)
    @OperationLog(operation = OperationType.UPDATE)
    @ApiOperation(value = "Update stream sink")
    public Response<Boolean> update(@Validated(UpdateValidation.class) @RequestBody SinkRequest request) {
        return Response.success(sinkService.update(request, LoginUserUtils.getLoginUser().getName()));
    }

    @RequestMapping(value = "/sink/delete/{id}", method = RequestMethod.DELETE)
    @OperationLog(operation = OperationType.DELETE)
    @ApiOperation(value = "Delete stream sink")
    @ApiImplicitParam(name = "id", dataTypeClass = Integer.class, required = true)
    public Response<Boolean> delete(@PathVariable Integer id) {
        boolean result = sinkService.delete(id, LoginUserUtils.getLoginUser().getName());
        return Response.success(result);
    }

}
