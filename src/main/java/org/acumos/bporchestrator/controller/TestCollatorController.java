/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */
package org.acumos.bporchestrator.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.acumos.bporchestrator.collator.service.ProtobufService;
import org.acumos.bporchestrator.collator.vo.Configuration;
import org.acumos.bporchestrator.collator.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class TestCollatorController {

	@Autowired
	@Qualifier("ProtobufServiceImpl")
	private ProtobufService protoService;

	private List<byte[]> listOfProtobufbytes;

	@ApiOperation(value = "Set the environment configuration.", response = Result.class)
	@RequestMapping(path = "/configCollator", method = RequestMethod.PUT)
	@ResponseBody
	public Object configureEnvironment(@RequestBody Configuration conf, HttpServletResponse response) {
		Result result = null;
		try {
			// 1. set the configuration
			protoService.setConf(conf);
			// 2. process protobuf
			protoService.processProtobuf();
			listOfProtobufbytes = new ArrayList<byte[]>();
			result = new Result(HttpServletResponse.SC_OK, "Environment configured successfully !!!");
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			result = new Result(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Error while setting Environment configuration");
		}
		return result;
	}

	@ApiOperation(value = "Dummy Data Collector.", response = Result.class)
	@RequestMapping(path = "/putData", method = RequestMethod.PUT)
	@ResponseBody
	public Object putData(@RequestParam(value = "messageName", required = true) String messageName,
			@RequestParam(value = "InputData", required = true) String input, HttpServletResponse response) {
		Object result = null;
		try {
			byte[] output = protoService.convertToProtobufFormat(messageName, input);
			listOfProtobufbytes.add(output);
			result = new Result(HttpServletResponse.SC_OK, "Data processed successfully !!!");
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new Result(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to convert to process data !!!");
		}
		return result;
	}

	@ApiOperation(value = "collateData return the output protobuf message", response = Result.class)
	@RequestMapping(path = "/collateData", method = RequestMethod.GET)
	@ResponseBody
	public Object collateData(HttpServletResponse response) {
		Object result = null;
		try {

			byte[] output = protoService.collateData(listOfProtobufbytes);
			result = protoService.readProtobufFormat("DataFrameRow", output);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			result = new Result(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Error while setting Environment configuration");
		}
		return result;
	}

}