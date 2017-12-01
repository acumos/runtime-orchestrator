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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.acumos.bporchestrator.exception.BORuntimeException;
import org.acumos.bporchestrator.model.*;
import org.acumos.bporchestrator.util.TaskManager;
import org.apache.commons.io.IOUtils;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Rest Controller that handles the API end points
 */
@RestController
public class BlueprintOrchestratorController {
	private static final Logger logger = LoggerFactory.getLogger(BlueprintOrchestratorController.class);

	private static Blueprint blueprint = null;
	private static DockerInfoList dockerList = null;
	private byte[] results = null;

	/**
	 * 
	 * @param binaryStream
	 *            This is the binary stream from the data source
	 * @param operation
	 *            This specifies the input_operation_signature the orchestrator
	 *            should invoke on the first node base on blueprint.json
	 * @return
	 */
	@ApiOperation(value = "operation on the first node in the chain", response = byte.class, responseContainer = "Page")
	@RequestMapping(path = "/{operation}", consumes = { "application/octet-stream" }, produces = {
			"application/octet-stream" }, method = RequestMethod.POST)
	public ResponseEntity<byte[]> notify(
			@ApiParam(value = "Inital request to start deploying... This binary stream is in protobuf format.", required = true) @Valid @RequestBody byte[] binaryStream,
			@ApiParam(value = "This operation should match with one of the input operation signatures in blueprint.json", required = true) @PathVariable("operation") String operation) {

		results = null;
		try {
			logger.info("Receiving /notify request: " + Arrays.toString(binaryStream));
			if (blueprint == null) {
				logger.error("Empty blueprint JSON");
				return new ResponseEntity<>(results, HttpStatus.PARTIAL_CONTENT);
			}
			if (dockerList == null) {
				logger.error("Need Docker Information... Exiting");
				return new ResponseEntity<>(results, HttpStatus.PARTIAL_CONTENT);
			}

			List<OperationSignature> inputs = blueprint.getInputs();
			List<Node> nodes = blueprint.getNodes();
			List<String> dependents = new ArrayList<>();
			for (Node node : nodes) {
				List<Component> dependsOn = node.getDependsOn();
				for (Component dependent : dependsOn) {
					String depName = dependent.getName();
					if (depName != null)
						dependents.add(depName);
				}
			}

			String urlBase;
			String url;
			byte[] output;
			for (OperationSignature os : inputs) {
				if (!os.getOperation().equals(operation))
					continue;

				for (Node node : nodes) {
					String container = node.getContainerName();
					if (dependents.contains(container))
						continue;
					DockerInfo d = dockerList.findDockerInfoByContainer(container);
					if (d == null) { // what to do if the deployer passed
										// incomplete docker info ???
						logger.error("Cannot find docker info about " + container);
						return new ResponseEntity<>(results, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					urlBase = "http://" + d.getIpAddress() + ":" + d.getPort() + "/";
					url = urlBase + os.getOperation();
					logger.info("Notifying first node " + container + " POST: " + url);
					output = httpPost(url, binaryStream);
					notifyNext(output, node);
				}
			}
		} catch (Exception ex) {
			logger.error("Notify failed", ex);
			return new ResponseEntity<>(results, HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	private void appendResults(byte[] readBytes) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		if (results != null)
			output.write(results);
		output.write(readBytes);
		results = output.toByteArray();
	}

	private byte[] notifyNext(byte[] input, Node current) throws Exception {
		String containerName = current.getContainerName();
		logger.info("Notifying dependents of " + containerName);
		List<Component> dependents = current.getDependsOn();
		if (dependents == null || dependents.isEmpty()) {
			logger.info("The container, " + containerName + ", has empty dependent");
			logger.info("Appending input of " + containerName + " to data sink");
			appendResults(input);
			return new byte[0];
		}
		String urlBase;
		String url;
		byte[] output;
		for (Component dependent : dependents) {
			String dcontainer = dependent.getName();
			DockerInfo dinfo = dockerList.findDockerInfoByContainer(dcontainer);
			if (dinfo == null) {
				logger.error("Cannot find docker info for " + dcontainer);
				throw new BORuntimeException("Incomplete docker info list");
			}
			urlBase = "http://" + dinfo.getIpAddress() + ":" + dinfo.getPort() + "/";

			OperationSignature os = dependent.getOperationSignature();
			if (os != null) {
				url = urlBase + os.getOperation();
				logger.info("Sending POST request to dependent " + dcontainer + " url " + url);
				output = httpPost(url, input);
				Node next = blueprint.getNodebyContainer(dcontainer);
				if (next == null) {
					logger.info("Reaching last node - " + dcontainer + " - in the path ");
					logger.info("Appending output of " + dcontainer + " to data sink");
					appendResults(output);
				} else
					notifyNext(output, next);

			} else {
				logger.error(dcontainer + " has empty Operation Signature");
			}

		}

		return new byte[0];
	}

	/**
	 * This is the end point for the deployer to push blueprint.json
	 * 
	 * @param blueprintReq
	 *            Holds blueprint.json
	 * @return HttpStatus.OK
	 */
	@ApiOperation(value = "Endpoint for the deployer to put blueprint JSON", response = Map.class, responseContainer = "Page")
	@RequestMapping(value = "/putBlueprint", consumes = { "application/json" }, method = RequestMethod.PUT)
	public static ResponseEntity<Map<String,String>> putBlueprint(
			@ApiParam(value = "Blueprint JSON", required = true) @Valid @RequestBody Blueprint blueprintReq) {
		logger.info("Receiving /putBlueprint request: " + blueprintReq.toString());

		TaskManager.setBlueprint(blueprintReq);
		blueprint = TaskManager.getBlueprint();
		
		logger.info("Returning HttpStatus.OK from putBlueprint");
		Map<String, String> results = new LinkedHashMap<>();
		results.put("status", "ok");
		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	/**
	 * The end point for the deployer to push docker info.
	 * 
	 * @param dockerListReq
	 *            Holds dockerInfo.json, containing IP addresses and port
	 *            numbers about all containers
	 * @return HttpStatus.OK
	 */
	@ApiOperation(value = "Endpoint for the deployer to push docker Info JSON", response = Map.class, responseContainer = "Page")
	@RequestMapping(value = "/putDockerInfo", consumes = { "application/json" }, method = RequestMethod.PUT)
	public static ResponseEntity<Map<String,String>> putDockerInfo(
			@ApiParam(value = "Docker Info JSON", required = true) @Valid @RequestBody DockerInfoList dockerListReq) {
		logger.info("Receiving /putDockerInfo request: " + dockerListReq.toString());

		TaskManager.setDockerList(dockerListReq);
		dockerList = TaskManager.getDockerList();
		
		logger.info("Returning HttpStatus.OK from putBlueprint");
		Map<String, String> results = new LinkedHashMap<>();
		results.put("status", "ok");

		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	private byte[] httpPost(String url, byte[] binaryStream) throws IOException {

		URL obj = new URL(url);

		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/octet-stream;");
		con.setDoOutput(true);
		OutputStream out = con.getOutputStream();
		out.write(binaryStream);
		out.flush();
		out.close();
		logger.info("HTTPS POST DeployerRequest Sent ::" + url);
		String responseMessage = con.getResponseMessage();
		logger.info("GOT RESPONSE Message " + responseMessage);
		int responseCode = con.getResponseCode();
		logger.info("GOT RESPONSE CODE " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) {
			return IOUtils.toByteArray(con.getInputStream());
		} else {
			logger.error("ERROR:::::::POST request did not work. " + url);
		}

		return new byte[0];

	}

}
