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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.acumos.bporchestrator.controller.DBResponseRunnable;
import org.acumos.bporchestrator.MCAttributes;
import org.acumos.bporchestrator.splittercollator.service.ProtobufService;
import org.acumos.bporchestrator.splittercollator.service.ProtobufServiceImpl;
import org.acumos.bporchestrator.splittercollator.service.SplitterProtobufService;
import org.acumos.bporchestrator.splittercollator.service.SplitterProtobufServiceImpl;
import org.acumos.bporchestrator.splittercollator.vo.Configuration;
import org.acumos.bporchestrator.model.*;
import org.acumos.bporchestrator.util.NewThreadAttributes;
import org.acumos.bporchestrator.util.TaskManager;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.acumos.bporchestrator.controller.FinalResults;

/**
 * Rest Controller that handles the API end points
 */
@RestController
// Uncomment @RequestMapping to enable swagger
// @RequestMapping(value = "/mc/blueprintorchestrator/")

public class BlueprintOrchestratorController {

	private static final Logger logger = LoggerFactory.getLogger(BlueprintOrchestratorController.class);
	private static final String DATABROKER = "Databroker";

	@Autowired
	@Qualifier("ProtobufServiceImpl")
	private ProtobufService protoService1;

	@Autowired
	@Qualifier("SplitterProtobufServiceImpl")
	private SplitterProtobufService protoService2;

	private static volatile byte[] finalOutput = null;
	static volatile boolean probePresent = false;

	/**
	 * The MC is triggerred by a /{operation} request. The requester also sends
	 * the protobuf binary message.
	 * 
	 * @param <T>
	 *            Generic
	 * @param binaryStream
	 *            This is the binary stream from the data source
	 * @param operation
	 *            This specifies the input_operation_signature of the runtime
	 *            orchestrator should invoke on the first node based on
	 *            blueprint.json
	 * @return The correct response on the error message
	 */

	@ApiOperation(value = "operation on the first node in the chain", response = byte.class, responseContainer = "Page")
	@RequestMapping(path = "/{operation}", method = RequestMethod.POST)
	public <T> ResponseEntity<T> notify(
			@ApiParam(value = "Inital request to start deploying... This binary stream is in protobuf format.", required = false) @Valid @RequestBody byte[] binaryStream,
			@ApiParam(value = "This operation should match with one of the input operation signatures in blueprint.json", required = true) @PathVariable("operation") String operation) {

		ExecutorService service3 = Executors.newFixedThreadPool(10);
		finalOutput = null;
		byte[] finalResults = null;
		Blueprint blueprint = TaskManager.getBlueprint();
		DockerInfoList dockerList = TaskManager.getDockerList();

		List<Node> nodeList = new ArrayList<Node>();
		nodeList = blueprint.getNodes();

		for (Node n : nodeList) {
			n.setNodeOutput(null);
			n.setOutputAvailable(false);
			n.setBeingProcessedByAThread(false);

		}

		// clear the connecttimeout models list
		if (TaskManager.getListofsocketimoutmodels() != null && !TaskManager.getListofsocketimoutmodels().isEmpty()) {
			TaskManager.getListofsocketimoutmodels().clear();
		}

		// clear the Final results
		if (TaskManager.getFinalResults() != null && (TaskManager.getFinalResults().getMsgname() != null)) {
			FinalResults f0 = TaskManager.getFinalResults();
			f0.setMsgname("Begin");
			f0.setFinalresults(null);
			TaskManager.setFinalResults(f0);
		}

		// Probe related.
		String probeContName = "Probe";
		String probeOperation = "data";
		String probeUrl = null;

		// Input node related
		String inpContainer = null;
		String inpOperation = null;

		// Bad execution message
		String badExecutionMsg = null;

		// On receiving this request, the data source is blocked.
		// We will need to unblock it only when we receive output from the first
		// model.

		// Check if blueprint and dockerList has been populated.
		try {
			logger.info("****************************************************************************");
			logger.info("notify: Receiving /{} request: {}", operation,
					(Arrays.toString(binaryStream)).substring(0, 20));
			if (blueprint == null) {
				logger.error("notify: Empty blueprint JSON");
				return (ResponseEntity<T>) new ResponseEntity<>(finalResults, HttpStatus.PARTIAL_CONTENT);

			}
			if (dockerList == null) {
				logger.error("notify: Need Docker Information... Exiting");
				return (ResponseEntity<T>) new ResponseEntity<>(finalResults, HttpStatus.PARTIAL_CONTENT);
			}

			List<InputPort> inps = blueprint.getInputPorts();
			byte[] output = null;

			// Check if Probe is present in the composite solution. If yes, get
			// its container name, and operation. THIS PROBE DATA WILL BE USED
			// FOR
			// THE DATA SOURCE CASE ONLY.

			ArrayList<ProbeIndicator> list_of_pb_indicators = blueprint.getProbeIndicator();
			for (ProbeIndicator pbindicator : list_of_pb_indicators) {
				if (pbindicator.getValue().equalsIgnoreCase("true")) {
					probePresent = true;
				}
			}

			/*
			 * // Find the url etc. for the probe if (probePresent == true) {
			 * probeUrl =
			 * constructURL(blueprint.getNodebyContainer(probeContName)); }
			 */

			// Now call the input node and its operation signature.
			for (InputPort inport : inps) {
				if (inport.getOperationSignature().getOperationName().equals(operation)) {
					inpContainer = inport.getContainerName();
					inpOperation = operation;
					break;

				}
			}

			String inpMsgName = null;

			Node inpNode = blueprint.getNodebyContainer(inpContainer);
			ArrayList<OperationSignatureList> inplosl = inpNode.getOperationSignatureList();
			for (OperationSignatureList inposl : inplosl) {
				inpMsgName = inposl.getOperationSignature().getInputMessageName();
				break;
			}

			// call contact node.
			String url = constructURL(inpNode);

			logger.info("Thread {} - Contacting node {}", Thread.currentThread().getId(), inpNode.getContainerName());
			byte[] inpNodeOutput = contactnode(binaryStream, url, inpNode.getContainerName());

			// set the output for input node
			inpNode.setNodeOutput(inpNodeOutput);

			// set outputAvailable for input node
			inpNode.setOutputAvailable(true);

			// call probe if required.
			/*
			 * if (probePresent == true) {
			 * 
			 * contactProbe(inpNodeOutput, probeUrl, probeContName,
			 * inpNode.getOperationSignatureList().get(0).getOperationSignature(
			 * ).getInputMessageName(), inpNode); }
			 */

			/*
			 * if (probePresent == true) { // create a dummydepsofdeps null
			 * object ArrayList<ConnectedTo> dummydepsofdeps = null;
			 * prepareAndContactprobe(inpOperation, probeUrl, inpContainer,
			 * binaryStream, output, dummydepsofdeps, singleModel);
			 * 
			 * }
			 */

			// Initial call to traverseEachNode
			logger.info("Calling traverseEachNode");

			NewThreadAttributes newThreadAttributes = new NewThreadAttributes();
			// set the all the required attributes.

			newThreadAttributes.setpNode(inpNode);
			newThreadAttributes.setsNode(null);
			newThreadAttributes.setOut(inpNodeOutput);
			newThreadAttributes.setId(0);
			newThreadAttributes.setPbNode(blueprint.getNodebyContainer(probeContName));

			// create a thread with the newThreadAttributes objects
			logger.info("Starting a new thread  with Node {} as predecessor", inpNode.getContainerName());

			service3.execute(new NewModelCaller(newThreadAttributes));

			// traverseEachNode(inpNode, null, inpNodeOutput, 0);

			while (true) {
				Thread.sleep(1000);
				/*
				 * //logger.info("Thread {} Inside while true. Final ouput is ",
				 * Thread.currentThread().getId(), finalOutput);
				 */
				if (finalOutput != null) {
					logger.info("Sending back to the Data Source {}", finalOutput);
					service3.shutdown();
					return (ResponseEntity<T>) new ResponseEntity<>(finalOutput, HttpStatus.OK);
				}
			}
			/*
			 * boolean singleModel = false;
			 * 
			 * // Check if the input node has any dependencies? if no, set //
			 * singlemodel to true. ArrayList<ConnectedTo> inpNodeDeps =
			 * findconnectedto(inpNode, inpOperation); if (inpNodeDeps == null
			 * || inpNodeDeps.isEmpty()) { singleModel = true; }
			 * 
			 * String inpUrl = constructURL(inpNode); try { output =
			 * contactnode(binaryStream, inpUrl, inpContainer); } catch
			 * (Exception e) { return (ResponseEntity<T>) new ResponseEntity<>(
			 * "Exception for" + " " + inpContainer + ":::" + e.getMessage(),
			 * HttpStatus.INTERNAL_SERVER_ERROR);
			 * 
			 * }
			 * 
			 * 
			 * // If output of the first model is null, stop execution and
			 * return // to the data source.
			 * 
			 * if (output == null || (output.length == 0)) { logger.error(
			 * "The output of {} is null. Model connector will not proceed.",
			 * inpContainer);
			 * 
			 * FinalResults f = new FinalResults(); f.setMsgname(
			 * "Error from container " + inpContainer);
			 * f.setFinalresults(output); TaskManager.setFinalResults(f);
			 * 
			 * badExecutionMsg = f.getMsgname(); return (ResponseEntity<T>) new
			 * ResponseEntity<>(badExecutionMsg,
			 * HttpStatus.INTERNAL_SERVER_ERROR);
			 * 
			 * }
			 * 
			 * 
			 * 
			 * if probe in composite solution send the input message of input
			 * //node to probe also and wait for http }
			 * 
			 * if (probePresent == true) {
			 * 
			 * // create a dummydepsofdeps null object ArrayList<ConnectedTo>
			 * dummydepsofdeps = null; prepareAndContactprobe(inpOperation,
			 * probeUrl, inpContainer, binaryStream, output, dummydepsofdeps,
			 * singleModel);
			 * 
			 * }
			 * 
			 * // If it is a single model, then return the correct output of the
			 * // single model. if (singleModel != true) {
			 * notifyNextNode(output, inpNode, inpOperation, probePresent,
			 * probeContName, probeOperation, probeUrl); } else { return
			 * (ResponseEntity<T>) new ResponseEntity<>(output, HttpStatus.OK);
			 * }
			 * 
			 */
		} // try ends for notify method's chunk
		catch (Exception ex) {
			logger.error("notify: Notify failed", ex);
			return (ResponseEntity<T>) new ResponseEntity<>(ex.toString(), HttpStatus.INTERNAL_SERVER_ERROR);

		}

		// if no exceptions in the notifyNextNode

		/*
		 * FinalResults f2 = TaskManager.getFinalResults(); finalResults =
		 * f2.getFinalresults(); badExecutionMsg = f2.getMsgname(); if
		 * ((badExecutionMsg).contains("Error")) { // Return error due to
		 * correct execution of notifyNextNode but // output null due to wrong
		 * model behaviours return (ResponseEntity<T>) new
		 * ResponseEntity<>(badExecutionMsg, HttpStatus.INTERNAL_SERVER_ERROR);
		 * } else { return (ResponseEntity<T>) new
		 * ResponseEntity<>(finalResults, HttpStatus.OK); }
		 * 
		 * 
		 */
	}

	/**
	 * @param output
	 *            The result of the previous node
	 * @param n
	 *            Current node
	 * @param oprn
	 *            Current node's operation which was called
	 * @param prbPresent
	 *            Indicator of probe presence in the blueprint
	 * @param prbcontainername
	 *            Probe's container name if Probe is present
	 * @param prboperation
	 *            Probe's operation name if Probe is present
	 * @param prbUrl
	 *            Probe's url if Probe is present
	 * @return byte[] output stream
	 * @throws Exception
	 *             : Exception
	 */
	/*
	 * public byte[] notifyNextNode(byte[] output, Node n, String oprn, boolean
	 * prbPresent, String prbcontainername, String prboperation, String prbUrl)
	 * throws Exception {
	 * 
	 * Blueprint blueprint = TaskManager.getBlueprint(); DockerInfoList
	 * dockerList = TaskManager.getDockerList();
	 * 
	 * // Find dependents of the node. If the dependent is null, then send the
	 * // output // message to the probe and stop looping.
	 * ArrayList<ConnectedTo> deps = null; ArrayList<ConnectedTo> depsOfDeps =
	 * null; try { deps = findconnectedto(n, oprn); } catch (Exception e) {
	 * logger.error("notifyNextNode: Finding Connected to failed", e); }
	 * 
	 * // First time the loop executes (i.e when we call 1st dependent node, //
	 * the value // of depoutput is output from previous databroker or input
	 * node. // Later on on every call it will be the output of the contactnode
	 * call // passed // as depoutput2) byte[] depOutput = output; byte[]
	 * depOutput2 = null;
	 * 
	 * // for loop here to loop through the dependents. for (ConnectedTo depItem
	 * : deps) {
	 * 
	 * String depContainerName = depItem.getContainerName(); String depOperation
	 * = depItem.getOperationSignature().getOperationName(); Node depNode =
	 * blueprint.getNodebyContainer(depContainerName);
	 * 
	 * // Find dependent's url and name for the node. Url finding will also //
	 * need the // dependent's operation DockerInfo d3 =
	 * dockerList.findDockerInfoByContainer(depContainerName);
	 * 
	 * if (d3 == null && !(depContainerName.contains("Splitter") ||
	 * depContainerName.contains("Collator"))) { // what to do if the deployer
	 * passed incomplete dockerInfo ??? logger.error(
	 * "Cannot find docker info about the container {}", depContainerName); }
	 * 
	 * // Contact the dependent node. if
	 * (!(((depNode.getNodeType()).equals("Splitter")) ||
	 * ((depNode.getNodeType()).equals("Collator")))) {
	 * 
	 * URIBuilder builder = new URIBuilder();
	 * builder.setScheme("http").setHost(d3.getIpAddress()).setPort(new
	 * Integer(d3.getPort()).intValue()) .setPath("/" + depOperation);
	 * 
	 * String depurl = builder.build().toURL().toString();
	 * 
	 * try { depOutput2 = contactnode(depOutput, depurl, depContainerName);
	 * 
	 * } catch (Exception ex) { logger.error(
	 * "Exception in notifyNextNode method while calling a dependent node {}",
	 * depContainerName + " ", ex); throw ex;
	 * 
	 * }
	 * 
	 * // If output of any model is null, Model connector stops // immediately
	 * and returns. if (depOutput2 == null || (depOutput2.length == 0)) {
	 * logger.error(
	 * "The output of {} is null. Model connector will not proceed.",
	 * depContainerName);
	 * 
	 * FinalResults f4 = new FinalResults(); f4.setMsgname(
	 * "Error from container " + depContainerName);
	 * f4.setFinalresults(depOutput2); TaskManager.setFinalResults(f4); return
	 * depOutput2;
	 * 
	 * }
	 * 
	 * }
	 * 
	 * // find dependents of the current dependent to see if we need to // send
	 * input msg // or both the input and output msg. try { depsOfDeps =
	 * findconnectedto(depNode, depOperation);
	 * 
	 * if ((depNode.getNodeType()).equals("Splitter")) { List<byte[]>
	 * mergedOutput;
	 * 
	 * // CALLING MULTIPLE MODELS WITH SPLITTER INPUT i.e // SPLITTER's
	 * PRECEDING NODE's // OUTPUT. // THIS NEEDS TO CHANGE IN PARAMETER BASED
	 * SPLITTER WHERE // ITS INPUT AND OUTPUT // WILL NOT BE THE SAME.
	 * 
	 * // CALL MULTIPLE MODELS SEQUENTIALLY // mergedoutput =
	 * callMultipleModels(depoutput, depsofdeps, // prburl,prbpresent);
	 * 
	 * // CALL MULTIPLE MODELS PARALLELY mergedOutput =
	 * callMultipleModelsParallelyClient(depOutput, depsOfDeps, prbUrl,
	 * prbPresent);
	 * 
	 * // Stop execution if any of the PARALLEL models had sent // null output
	 * or not responded. FinalResults f5 = TaskManager.getFinalResults(); if (f5
	 * != null && f5.getMsgname() != null) { String executionStatusMsg =
	 * f5.getMsgname(); if ((executionStatusMsg.contains("Error"))) {
	 * 
	 * // Return from here. return null; } } // whether splitter has collator
	 * reference or not depsOfDeps = linkRelatedCollator(depsOfDeps);
	 * 
	 * // configure collator here String collatorContainer =
	 * depsOfDeps.get(0).getContainerName(); Node collatorNode =
	 * blueprint.getNodebyContainer(collatorContainer); String
	 * containerOperation =
	 * collatorNode.getOperationSignatureList().get(0).getOperationSignature()
	 * .getOperationName(); String collatorType =
	 * collatorNode.getCollatorMap().getCollatorType(); String protobufFileStr =
	 * collatorNode.getCollatorMap().getOutputMessageSignature(); logger.info(
	 * "set all the configuration related values for the collator is started");
	 * Configuration configuration = new Configuration();
	 * logger.info("collatortype" + collatorType);
	 * configuration.setCollator_type(collatorType);
	 * logger.info("protobufFileStr" + protobufFileStr);
	 * 
	 * configuration.setProtobufFileStr(protobufFileStr);
	 * protoService.setConf(configuration); try {
	 * protoService.processProtobuf(); } catch (Exception ex) { logger.error(
	 * "Exception in processProtobuf()", ex); }
	 * 
	 * logger.info(
	 * "set all the configuration related values for the collator is ended");
	 * 
	 * // call collator // ProtobufService protobufService1 = new //
	 * ProtobufServiceImpl();
	 * 
	 * // REAL CALL logger.info("Call the protoService.collateData api");
	 * 
	 * try { if (null != mergedOutput && !mergedOutput.isEmpty()) { logger.info(
	 * "collate data input" + mergedOutput); depOutput2 =
	 * protoService.collateData(mergedOutput); logger.info("collate data output"
	 * + depOutput2); } else { logger.debug(
	 * "Please check final combine result from multiple models are working or not"
	 * ); }
	 * 
	 * } catch (Exception ex) { logger.error(
	 * "calling collateData method of the Collator failed", ex); }
	 * 
	 * // SIMULATED CALL // depoutput2 =
	 * httpPost("http://127.0.0.1:9222/collate%22, // output);
	 * 
	 * // Set depnode to collator node before call notifyNextNode // recursively
	 * depNode = collatorNode; depOperation = containerOperation;
	 * depContainerName = collatorContainer;
	 * 
	 * }
	 * 
	 * }
	 * 
	 * catch (Exception e1) { logger.error("notifyNextNode failed", e1); throw
	 * e1; }
	 * 
	 * if (prbPresent == true && !(((depContainerName.contains("Splitter")) ||
	 * (depContainerName.contains("Collator"))))) {
	 * 
	 * // For any node (if probe is present), contact probe with input //
	 * message // For the last node(if probe is present), contact probe with //
	 * input message name // & contact probe with output message name
	 * 
	 * prepareAndContactprobe(depOperation, prbUrl, depContainerName, depOutput,
	 * depOutput2, depsOfDeps, false);
	 * 
	 * }
	 * 
	 * if (depsOfDeps.isEmpty()) { FinalResults f3 = new FinalResults(); // Set
	 * the correct output. Set the msg to be success. // The output is the
	 * output of the composite solution upon // correct execution.
	 * f3.setMsgname("success"); f3.setFinalresults(depOutput2);
	 * TaskManager.setFinalResults(f3); }
	 * 
	 * // Call notifyNextNode function if (!(depsOfDeps.isEmpty())) { byte[]
	 * mdOutput = notifyNextNode(depOutput2, depNode, depOperation, prbPresent,
	 * prbcontainername, prboperation, prbUrl); if (mdOutput == null ||
	 * mdOutput.length == 0) { return null; }
	 * 
	 * } } return output; }
	 */
	/**
	 * Contacting probe
	 *
	 * @param probeUrl
	 *            : url of the probe
	 * @param binaryStream
	 *            : input protobuf binary message.
	 * @param probeUrl
	 *            : url of probe
	 * @param msgName
	 *            : input message name
	 * @param n
	 *            : the node object
	 * @param prbCname
	 *            : probe container name
	 *
	 * @return : returns byte[] type
	 * @throws IOException
	 *             : IO exception
	 */

	public byte[] contactProbe(byte[] binaryStream, String probeUrl, String prbCname, String msgName, Node n)
			throws Exception {

		Blueprint blueprint = TaskManager.getBlueprint();
		DockerInfoList dockerList = TaskManager.getDockerList();

		byte[] output2;
		Node probeNode = blueprint.getNodebyContainer(prbCname);
		logger.info("contactProbe: Thread {} : Notifying probe: {}, POST: {}, proto uri: {} , message Name: {}",
				Thread.currentThread().getId(), prbCname, probeUrl, n.getProtoUri(), msgName);

		output2 = httpPost(probeUrl, binaryStream, n.getProtoUri(), msgName);
		return output2;
	}

	/*
	 * void prepareAndContactprobe(String depOperation, String prbUrl, String
	 * depContainerName, byte[] depOutput, byte[] depOutput2,
	 * ArrayList<ConnectedTo> depsOfDeps, boolean singleModel) {
	 * 
	 * // Call notifyprobe if indicator is true. // Message name needed for
	 * sending to probe. (Find the input message // name for // the node. This
	 * will be needed later if probe is present.)
	 * 
	 * Blueprint blueprint = TaskManager.getBlueprint(); DockerInfoList
	 * dockerInfoList = TaskManager.getDockerList(); String depInpMsgName =
	 * null; String depoutMsgName = null; byte[] probeOut = null; byte[]
	 * probeOut2 = null; Node depNode =
	 * blueprint.getNodebyContainer(depContainerName);
	 * ArrayList<OperationSignatureList> deplosl =
	 * depNode.getOperationSignatureList(); for (OperationSignatureList deposl :
	 * deplosl) { // checking if the operation is the same as requested
	 * operation. // Only then // proceed to set the depinpmsgname and
	 * depoutmsgname accordingly. if
	 * ((deposl.getOperationSignature().getOperationName()).equals(depOperation)
	 * )
	 * 
	 * { depInpMsgName = deposl.getOperationSignature().getInputMessageName();
	 * depoutMsgName = deposl.getOperationSignature().getOutputMessageName();
	 * break; }
	 * 
	 * }
	 * 
	 * 
	 * If probe is present- For first node (with dummydepsofdeps = null) or any
	 * node with depsofdeps not empty, contact probe with input message
	 * 
	 * 
	 * if (((depsOfDeps == null) || !(depsOfDeps.isEmpty())) && singleModel ==
	 * false) { try { logger.info(
	 * "notifyNextNode: Thread {} : Notifying PROBE for node name: {}, inp msg name: {} , msg: {}"
	 * , Thread.currentThread().getId(), depContainerName, depInpMsgName,
	 * (Arrays.toString(depOutput)).substring(0, 20));
	 * 
	 * probeOut = contactProbe(depOutput, prbUrl, depContainerName,
	 * depInpMsgName, depNode); } catch (Exception e) { logger.error(
	 * "Contacting probe failed", e); } }
	 * 
	 * // If probe is present -For the last node contact probe with input //
	 * message name // & contact probe with output message name if (((depsOfDeps
	 * != null) && (depsOfDeps.isEmpty())) || (singleModel == true)) {
	 * 
	 * try {
	 * 
	 * logger.info(
	 * "notifyNextNode: Thread {} : Notifying PROBE for node name: {}, inp msg name: {} , msg: {}"
	 * , Thread.currentThread().getId(), depContainerName, depInpMsgName,
	 * (Arrays.toString(depOutput)).substring(0, 20));
	 * 
	 * probeOut = contactProbe(depOutput, prbUrl, depContainerName,
	 * depInpMsgName, depNode); } catch (Exception e) { logger.error(
	 * "notifyNextNode: Contacting probe failed", e); } try { // Note here
	 * depoutput2 is assigned to local depoutputof this // function.
	 * logger.info(
	 * "notifyNextNode: Thread {} : Notifying PROBE for node name: {}, out msg name: {} , msg: {}"
	 * , Thread.currentThread().getId(), depContainerName, depoutMsgName,
	 * (Arrays.toString(depOutput2)).substring(0, 20));
	 * 
	 * probeOut2 = contactProbe(depOutput2, prbUrl, depContainerName,
	 * depoutMsgName, depNode); } catch (Exception e) { logger.error(
	 * "notifyNextNode: Contacting probe failed", e); }
	 * 
	 * }
	 * 
	 * }
	 */

	/**
	 * Contacting a node
	 *
	 * @param binaryStream
	 *            : binary message
	 * @param url
	 *            : url of the data broker
	 * @param name
	 *            : url of the databroker
	 * @return byte[] : received protobuf message
	 * @throws Exception
	 *             : Exception
	 */

	public byte[] contactnode(byte[] binaryStream, String url, String name) throws Exception {

		logger.info("contactnode: Thread {} : Notifying node: {}, POST: {}", Thread.currentThread().getId(), name, url);

		byte[] output4 = null;
		try {
			output4 = httpPost(url, binaryStream);
		} catch (Exception e) {
			logger.error("contactnode: Contacting node failed", e);
			throw e;
		}
		return output4;
	}

	/**
	 * Contacting data broker
	 *
	 * @param db_url
	 *            : url of the data broker
	 * @param db_c_name
	 *            : url of the databroker
	 * @return byte[] : received protobuf message
	 * @throws Exception
	 *             : Exception
	 */

	public byte[] contactDataBroker(String db_url, String db_c_name) throws Exception {

		// Find the databroker script
		Blueprint blueprint = TaskManager.getBlueprint();
		Node dbnode = blueprint.getNodebyContainer(db_c_name);

		logger.info("contactDataBroker: Thread {} : Notifying databroker: {}, POST: {}", Thread.currentThread().getId(),
				db_c_name, db_url);

		byte[] output3 = null;

		output3 = httpGet(db_url);

		// Print out partial output from Data Broker
		logger.info("contactDataBroker: Thread " + Thread.currentThread().getId() + " : " + output3.length
				+ " bytes read from DataBroker");

		return output3;
	}

	/**
	 * Find connectedTo given a Node and its OperationSgnatureList data
	 * structure.
	 *
	 * @param n
	 *            : Node
	 * @param sent_ops
	 *            : Operation name.
	 * @return : returns byte[] type
	 *
	 * @throws Exception
	 *             : Exception
	 */

	public ArrayList<ConnectedTo> findconnectedto(Node n, String sent_ops) throws Exception {

		ArrayList<OperationSignatureList> listOfOperationSigList = n.getOperationSignatureList();
		for (OperationSignatureList opsl2 : listOfOperationSigList) {
			if ((opsl2.getOperationSignature().getOperationName()).equals(sent_ops)) {
				return opsl2.getConnectedTo();

			}
		}
		return null; // there will never be an occasion where we will return
		// null. Even in the case
		// where no dependents, the Array list of type connectedTo will be
		// returned by
		// the inner return. But in this case, the Arraylist will be empty and
		// have no
		// connectedTo objects.
	}

	public boolean allNodesOutputsAvailable() {

		Blueprint blueprint = TaskManager.getBlueprint();
		List<Node> listOfPredecessorName = new ArrayList<Node>();
		listOfPredecessorName = blueprint.getNodes();
		// Check all ancestors for output
		for (Node n : listOfPredecessorName)

		{
			if (n.isOutputAvailable() == false) {
				return false;
			}
		}

		return true;
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
	public ResponseEntity<Map<String, String>> putBlueprint(
			@ApiParam(value = "Blueprint JSON", required = true) @Valid @RequestBody Blueprint blueprintReq) {
		logger.info("****************************************************************************");
		logger.info("Receiving /putBlueprint request: {}", blueprintReq.toString());

		TaskManager.setBlueprint(blueprintReq);
		Blueprint blueprint = TaskManager.getBlueprint();
		List<Node> listOfPredecessorName = new ArrayList<Node>();
		List<Node> listofSuccessorName = new ArrayList<Node>();
		probePresent = false; // this will be set if probe is present - when
								// every operation request comes

		// both the below lists unsorted.
		listOfPredecessorName = blueprint.getNodes();
		listofSuccessorName = blueprint.getNodes();

		// create Matrix
		int[][] sourceDestinatioNodeMappingTable = new int[listOfPredecessorName.size()][listofSuccessorName.size()];
		for (int i = 0; i < listOfPredecessorName.size(); i++) {
			Node node = listOfPredecessorName.get(i);
			String predecessorNodeName = node.getContainerName();
			for (int j = 0; j < listofSuccessorName.size(); j++) {
				Node node1 = listofSuccessorName.get(j);
				String successorNodeName = node1.getContainerName();
				if (predecessorNodeName.equals(successorNodeName)) {
					sourceDestinatioNodeMappingTable[i][j] = 0;
					continue;
				} else {
					if (checkConnectedNode(node, successorNodeName)) {
						System.out.println(predecessorNodeName + "->" + successorNodeName);
						sourceDestinatioNodeMappingTable[i][j] = 1;
					} else {
						sourceDestinatioNodeMappingTable[i][j] = 0;
					}
				}
			}
		}

		// Create and immediateAncestors attribute for all the nodes. It will be
		// of type List<Node> as described in Node.java POJO.

		// Input node related
		String inpContainerName = null;
		String inpOperation = null;

		for (int j = 0; j < listofSuccessorName.size(); j++) {
			Node node1 = listofSuccessorName.get(j);

			for (int i = 0; i < listOfPredecessorName.size(); i++) {
				Node node = listOfPredecessorName.get(i);
				if (sourceDestinatioNodeMappingTable[i][j] == 1) {

					// if for a column, any row contains 1 i.e that row is an
					// immediate ancestor.
					// So, add that node to the current node's immediate
					// ancestor list.
					System.out.println(node1.getContainerName() + "'s ancestor is " + node.getContainerName());
					node1.getImmediateAncestors().add(node);

				}
			}
		}

		TaskManager.setSourceDestinatioNodeMappingTable(sourceDestinatioNodeMappingTable);

		logger.info("putBluePrint: Returning HttpStatus.OK from putBlueprint");
		Map<String, String> results = new LinkedHashMap<>();
		results.put("status", "ok");
		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	private boolean checkConnectedNode(Node node, String containerName) {
		ArrayList<OperationSignatureList> operationSignatureList = node.getOperationSignatureList();
		if (operationSignatureList != null) {
			for (OperationSignatureList operationSignatureListInstance : operationSignatureList) {
				ArrayList<ConnectedTo> connectedList = operationSignatureListInstance.getConnectedTo();
				if (connectedList != null && !connectedList.isEmpty()) {
					for (ConnectedTo connectedTo : connectedList) {
						if (connectedTo != null) {
							if (containerName.equals(connectedTo.getContainerName())) {
								return true;
							}
						}
					}

				}
			}
		}
		return false;
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
	public ResponseEntity<Map<String, String>> putDockerInfo(
			@ApiParam(value = "Docker Info JSON", required = true) @Valid @RequestBody DockerInfoList dockerListReq) {
		logger.info("****************************************************************************");
		logger.info("Receiving /putDockerInfo request: {}", dockerListReq.toString());

		Map<String, String> dbresults = new LinkedHashMap<>();
		TaskManager.setDockerList(dockerListReq);

		try {

			Blueprint blueprint = TaskManager.getBlueprint();
			DockerInfoList dockerList = TaskManager.getDockerList();

			// Check if blueprint and dockerList has been populated.

			if (blueprint == null) {
				logger.error("putDockerInfo: Empty blueprint JSON");
				return new ResponseEntity<>(dbresults, HttpStatus.PARTIAL_CONTENT);
			}
			if (dockerList == null) {
				logger.error("putDockerInfo: Need Docker Information... Exiting");
				return new ResponseEntity<>(dbresults, HttpStatus.PARTIAL_CONTENT);
			}

			List<Node> allnodes = blueprint.getNodes();

			// DataBroker related.
			boolean dataBrokerPresent = false;
			String dataBrokerContName = null;
			ArrayList<OperationSignatureList> ListOfDataBrokerOpSigList = null;
			String dataBrokerOperation = null;

			// Probe related.
			boolean probePresent = false;
			String probeContName = "Probe";
			String probeOperation = "data";
			String probeurl = null;

			// Check if data broker is present in the composite solution. If
			// yes, get its
			// container name, and operation.
			for (Node nd : allnodes) {
				if (nd.getNodeType().equalsIgnoreCase(DATABROKER)) {
					dataBrokerPresent = true;
					// get its container name : to be used later.
					dataBrokerContName = nd.getContainerName();
					// getting its operation name : to be used later ...OR is it
					// always get_image?
					ListOfDataBrokerOpSigList = nd.getOperationSignatureList();
					for (OperationSignatureList dbosl : ListOfDataBrokerOpSigList) {
						dataBrokerOperation = dbosl.getOperationSignature().getOperationName(); // here
						/*
						 * we are assuming Databroker will have only 1
						 * operation. this can be changed.
						 */
					}
					break;
				}
			}

			String databrokerurl = null;
			byte[] output = null;

			// Find the url etc. for this data broker.
			if (dataBrokerPresent == true) {
				DockerInfo d1 = dockerList.findDockerInfoByContainer(dataBrokerContName);

				if (d1 == null) { // what to do if the deployer passed
					// incomplete docker info ???
					logger.error("putDockerInfo: Cannot find docker info about the data broker {}", dataBrokerContName);
					dbresults.put("status", "error");
					return new ResponseEntity<>(dbresults, HttpStatus.INTERNAL_SERVER_ERROR);
				}

				URIBuilder builder = new URIBuilder();
				builder.setScheme("http").setHost(d1.getIpAddress()).setPort(new Integer(d1.getPort()).intValue())
						.setPath("/" + dataBrokerOperation); // Is this always
				// get_image?

				databrokerurl = builder.build().toURL().toString();
			}

			/*
			 * Check if Probe is present in the composite solution. If yes, get
			 * its container name, and operation. THIS PROBE DATA WILL BE USED
			 * FOR THE DATA BROKER CASE ONLY. Checking here again because
			 * requests back to back where taking old probe related values when
			 * probe related values were made class fields.
			 */

			/*
			 * IN CASE OF DATA SOURCE, call to /putDockerInfo will initialize
			 * the incorrect values. Then the notify function will initialize
			 * the correct values.
			 * 
			 */

			ArrayList<ProbeIndicator> list_of_pb_indicators = blueprint.getProbeIndicator();
			for (ProbeIndicator pbindicator : list_of_pb_indicators) {
				if (pbindicator.getValue().equalsIgnoreCase("true")) {
					probePresent = true;
				}
			}

			// Find the url etc. for the probe
			if (probePresent == true) {
				DockerInfo d2 = dockerList.findDockerInfoByContainer(probeContName);

				if (d2 == null) { // what to do if the deployer passed
					// incomplete docker info ???
					logger.error("putDockerInfo: Cannot find docker info about the probe {}", probeContName);
					return new ResponseEntity<>(dbresults, HttpStatus.INTERNAL_SERVER_ERROR);
				}

				URIBuilder builder = new URIBuilder();
				builder.setScheme("http").setHost(d2.getIpAddress()).setPort(new Integer(d2.getPort()).intValue())
						.setPath("/" + probeOperation);

				probeurl = builder.build().toURL().toString();
			}

			/*
			 * if Databroker in composite solution { make a POST request to data
			 * broker which will send the message. This is your message}
			 */

			Node dbnode = blueprint.getNodebyContainer(dataBrokerContName);
			if (dataBrokerPresent == true) {
				ExecutorService service2 = Executors.newFixedThreadPool(1);
				// make a GET request to the databroker and receive response
				do {
					output = contactDataBroker(databrokerurl, dataBrokerContName);
					if ((output != null) && (output.length != 0)) {
						MCAttributes mcAttributes = new MCAttributes();
						// set the all the required attributes.
						mcAttributes.setOutput(output);
						mcAttributes.setCurrentNode(dbnode);
						mcAttributes.setCurrentOperation(dataBrokerOperation);
						mcAttributes.setProbePresent(probePresent);
						mcAttributes.setProbeContName(probeContName);
						mcAttributes.setProbeOperation(probeOperation);
						mcAttributes.setProbeUrl(probeurl);
						// create a thread with the mcAttribute objects
						service2.execute(new DBResponseRunnable(mcAttributes));
					}

				} while ((output != null) && (output.length != 0));
			}
			dbresults.put("status", "ok");
			logger.info("putDockerInfo: Thread {} : Returning HttpStatus.OK from putDockerInfo",
					Thread.currentThread().getId());
			return new ResponseEntity<>(dbresults, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error("putDockerInfo(): failed to put dockerInfo: " + ex);
			return new ResponseEntity<>(dbresults, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * Sending HTTP POST request to Models
	 *
	 * @param url
	 *            : url to post to
	 * @param binaryStream
	 *            : the binary message
	 * @return: returns byte[]
	 * @throws IOException
	 *             : IO exception
	 */
	private byte[] httpPost(String url, byte[] binaryStream) throws Exception {
		return httpPost(url, binaryStream, null, null);
	}

	/**
	 * Sending HTTP POST request. Used for calling Probe and Models.
	 *
	 * @param url
	 *            : url of the node
	 * @param binaryStream
	 *            : the binary message
	 * @param protoUrl
	 *            : probe specific data
	 * @param messageName
	 *            : probe specific data
	 * @return: returns byte[] type
	 * @throws IOException
	 *             : IOException
	 */
	private byte[] httpPost(String url, byte[] binaryStream, String protoUrl, String messageName) throws Exception {

		URL obj = new URL(url);

		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/octet-stream;");
		con.setConnectTimeout(20000);
		if (protoUrl != null)
			con.setRequestProperty("PROTO-URL", protoUrl);
		if (messageName != null)
			con.setRequestProperty("Message-Name", messageName);
		con.setDoOutput(true);
		OutputStream out = con.getOutputStream();
		out.write(binaryStream);
		out.flush();
		out.close();
		logger.info("httpPost: Thread {}: HTTPS POST Sent {}", Thread.currentThread().getId(), url);
		String responseMessage = con.getResponseMessage();
		logger.info("httpPost: Thread {}: GOT RESPONSE Message {}", Thread.currentThread().getId(), responseMessage);
		int responseCode = con.getResponseCode();
		logger.info("httpPost: Thread {}: GOT RESPONSE CODE {}", Thread.currentThread().getId(), responseCode);
		if ((responseCode == HttpURLConnection.HTTP_OK) || (responseCode == HttpURLConnection.HTTP_CREATED)
				|| (responseCode == HttpURLConnection.HTTP_ACCEPTED)
				|| (responseCode == HttpURLConnection.HTTP_ACCEPTED)
				|| (responseCode == HttpURLConnection.HTTP_NOT_AUTHORITATIVE)
				|| (responseCode == HttpURLConnection.HTTP_NO_CONTENT) || (responseCode == HttpURLConnection.HTTP_RESET)
				|| (responseCode == HttpURLConnection.HTTP_PARTIAL)) {
			return IOUtils.toByteArray(con.getInputStream());
		} else {
			logger.error(
					"httpPost: Thread {}: ERROR:::POST request did not work for {}. Got the following response: {} {}",
					Thread.currentThread().getId(), url, responseCode, responseMessage);
		}

		return new byte[0];

	}

	/*
	 * Sending HTTP GET request to the Data Broker. This reads from
	 * HTTPURLConnection into the memory then return the contents of the memory.
	 * 
	 */
	private byte[] httpGet(String url) throws Exception {
		HttpURLConnection con = null;
		InputStream in = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		int count;
		try {
			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(20000);
			int responseCode = con.getResponseCode();
			logger.info("httpGet(): GET Response Code ::" + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				in = con.getInputStream();
				byte[] buffer = new byte[8192];
				while ((count = in.read(buffer)) > 0)
					out.write(buffer, 0, count);

				return out.toByteArray();
			} else {
				logger.error(
						"httpGet: Thread {}: ERROR:::GET request did not work {}. Got the following response code: {}",
						Thread.currentThread().getId(), url, responseCode);
				throw new Exception("GET " + url + " request did not work");
			}
		} finally {
			if (in != null)
				in.close();

			out.close();
		}
	}

	// Read the Matrix and connect the models.
	public synchronized void traverseEachNode(Node node, Node secondNode, byte[] out, int identifier, Node probeNode)
			throws Exception {

		ExecutorService service4 = Executors.newFixedThreadPool(50);
		Blueprint blueprint = TaskManager.getBlueprint();
		List<Node> listOfPredecessorName = new ArrayList<Node>();
		List<Node> listofSuccessorName = new ArrayList<Node>();
		listOfPredecessorName = blueprint.getNodes();

		DockerInfoList dockerList = TaskManager.getDockerList();

		int i = 0;
		// if identifier is not zero i.e call is from inside.
		// Then set i i.e predecessorNode as the new incoming node.
		// Else the for loop will start from i=0 i.e first node.

		i = i + listOfPredecessorName.indexOf(node);

		for (; i < listOfPredecessorName.size(); i++) {

			Node predecessorNode = listOfPredecessorName.get(i);
			String predecessorNodeName = predecessorNode.getContainerName();

			// if secondNode is null, that means the call is NOT from Splitter
			// -> Iterate usually over the entire inner for loop

			// if secondNode is not null, that means the call is from Splitter
			// -> Only iterate over the secondNode.

			if (secondNode == null) {
				listofSuccessorName = blueprint.getNodes();
			} else {
				listofSuccessorName.add(secondNode);
			}

			for (int j = 0; j < listofSuccessorName.size(); j++) {

				Node successorNode = listofSuccessorName.get(j);
				String successorNodeName = successorNode.getContainerName();

				if (TaskManager.getSourceDestinatioNodeMappingTable()[i][j] == 1) {

					// Only execute this if the successor's output is not
					// available.
					// And its immediate Ancestors output is Available.
					if (successorNode.immediateAncestorsOutputAvailable() && !successorNode.isOutputAvailable()
							&& !successorNode.beingProcessedByAThread) {

						successorNode.beingProcessedByAThread = true;
						logger.info("********  Thread {} is PROCESSING {} -> {} ********",
								Thread.currentThread().getId(), predecessorNodeName, successorNodeName);

						// Splitter node case
						if (successorNodeName.contains("Splitter")) {

							// call local splitter function. call the
							// correct
							// function based on array based or copy based
							// splitter.

							// set output for the splitter inside it
							// successorNode.setNodeOutput(null);

							if (successorNode.getSplitterMap().getSplitter_type().equalsIgnoreCase("Copy-based")) {
								// successor is splitter

								// For copy based splitter, the output is the
								// same as its input i.e previous model's
								// output.
								successorNode
										.setNodeOutput(successorNode.getImmediateAncestors().get(0).getNodeOutput());

								// set the outputAvailable for the splitter
								// inside it
								successorNode.setOutputAvailable(true);

								// call all its children on separate threads.

								List<ConnectedTo> listOfNodesConnectedToSplitter = findconnectedto(successorNode,
										successorNode.getOperationSignatureList().get(0).getOperationSignature()
												.getOperationName());
								for (ConnectedTo connectedTo : listOfNodesConnectedToSplitter) {

									String connectedToContainer = connectedTo.getContainerName();
									Node connectedToNode = blueprint.getNodebyContainer(connectedToContainer);

									// spawn child node threads
									NewThreadAttributes newThreadAttributes = new NewThreadAttributes();

									// set the all the required attributes.
									newThreadAttributes.setpNode(successorNode);
									newThreadAttributes.setsNode(connectedToNode);
									newThreadAttributes.setOut(successorNode.getNodeOutput());
									newThreadAttributes.setId(1);
									newThreadAttributes.setPbNode(probeNode);

									// create a thread with the
									// newThreadAttributes objects
									logger.info("Starting a new thread  with Node {} as predecessor",
											successorNode.getContainerName());

									service4.execute(new NewModelCaller(newThreadAttributes));

								}

							}

							if (successorNode.getSplitterMap().getSplitter_type().equalsIgnoreCase("Parameter-based")) {
								// Setting a dummy output in the SPlitter node
								// in case of Parameter based splitter
								successorNode
										.setNodeOutput(successorNode.getImmediateAncestors().get(0).getNodeOutput());

								// set the outputAvailable for the splitter
								// inside it
								successorNode.setOutputAvailable(true);

								// call all its children on separate threads.
								SplitterMap modelSplitterMap = successorNode.getSplitterMap();

								org.acumos.bporchestrator.splittercollator.vo.SplitterMap splitterMap = new org.acumos.bporchestrator.splittercollator.vo.SplitterMap();
								splitterMap.setSplitter_type(modelSplitterMap.getSplitter_type());
								splitterMap.setInput_message_signature(modelSplitterMap.getInput_message_signature());
								splitterMap.setMap_inputs(modelSplitterMap.getMap_inputs());
								splitterMap.setMap_outputs(modelSplitterMap.getMap_outputs());

								SplitterProtobufService protoServiceParameterSpl = new SplitterProtobufServiceImpl();

								try {
									logger.info("Calling Parameter based Splitter's setconf with splittermap {}",
											splitterMap);
									protoServiceParameterSpl.setConf(splitterMap);
								} catch (Exception e) {
									logger.error("Exception {} in calling setConf for Parameter Based Splitter", e);
									logger.error("SplitterMap value was {}", splitterMap);

								}

								Map<String, Object> paramSplitterOutput = new HashMap<String, Object>();
								try {

									paramSplitterOutput = protoServiceParameterSpl.parameterBasedSplitData(
											successorNode.getImmediateAncestors().get(0).getNodeOutput());
								} catch (Exception e) {
									logger.info("Exception {} in calling parameterBasedSplitData for Splitter", e);
									logger.error("Input List was ",
											successorNode.getImmediateAncestors().get(0).getNodeOutput());

								}
								List<ConnectedTo> listOfNodesConnectedToSplitter = findconnectedto(successorNode,
										successorNode.getOperationSignatureList().get(0).getOperationSignature()
												.getOperationName());
								for (ConnectedTo connectedTo : listOfNodesConnectedToSplitter) {

									String connectedToContainer = connectedTo.getContainerName();
									Node connectedToNode = blueprint.getNodebyContainer(connectedToContainer);

									NewThreadAttributes newThreadAttributes = new NewThreadAttributes();
									// set the all the required attributes.

									newThreadAttributes.setpNode(successorNode);
									newThreadAttributes.setsNode(connectedToNode);
									newThreadAttributes
											.setOut((byte[]) (paramSplitterOutput.get(connectedToContainer)));
									newThreadAttributes.setId(1);
									newThreadAttributes.setPbNode(probeNode);

									// create a thread with the
									// newThreadAttributes
									// objects
									logger.info("Starting a new thread  with Node {} as predecessor",
											successorNode.getContainerName());
									service4.execute(new NewModelCaller(newThreadAttributes));

								}

							}

							// call probe if required.

							if (finalOutput == null) {
								continue;
							} else
								break;
						}

						// Collator node case
						else if (successorNodeName.contains("Collator")) {

							byte[] collatorOutput = null;

							if (successorNode.getCollatorMap().getCollator_type().equalsIgnoreCase("Array-based"))

							{
								CollatorMap modelCollatorMap = successorNode.getCollatorMap();
								org.acumos.bporchestrator.splittercollator.vo.CollatorMap collatorMap = new org.acumos.bporchestrator.splittercollator.vo.CollatorMap();
								collatorMap.setCollator_type(modelCollatorMap.getCollator_type());
								collatorMap.setOutput_message_signature(modelCollatorMap.getOutput_message_signature());
								collatorMap.setMap_inputs(modelCollatorMap.getMap_inputs());
								collatorMap.setMap_outputs(modelCollatorMap.getMap_outputs());

								ProtobufService protoServiceArrbased = new ProtobufServiceImpl();

								try {
									logger.info("Calling setConf for Array-based Collator with collatorMap {}",
											collatorMap);
									protoServiceArrbased.setConf(collatorMap);
								} catch (Exception e) {
									logger.error("Exception {} in calling setConf for Array based Collator", e);
									logger.error("Collator Map was", collatorMap);
								}

								// protobuf string required to be set???

								// creates a list of outputs needed by Collatpr
								// input API
								List<Node> ancestors = successorNode.getImmediateAncestors();
								List<byte[]> arrayCollateInput = new ArrayList<byte[]>();
								for (Node n : ancestors) {

									arrayCollateInput.add(n.getNodeOutput());
								}

								// call collate Data
								try {
									collatorOutput = protoServiceArrbased.arrayBasedCollateData(arrayCollateInput);
								} catch (Exception e) {
									logger.error("Exception in calling arrayBasedCollateData {}", e);
									logger.error("Input List was {}", arrayCollateInput);
								}
								// set output for Collator
								successorNode.setNodeOutput(collatorOutput);

								// set output available for Collator
								successorNode.setOutputAvailable(true);

							}

							if (successorNode.getCollatorMap().getCollator_type().equalsIgnoreCase("Parameter-based"))

							{
								CollatorMap modelCollatorMap = successorNode.getCollatorMap();
								org.acumos.bporchestrator.splittercollator.vo.CollatorMap collatorMap = new org.acumos.bporchestrator.splittercollator.vo.CollatorMap();
								collatorMap.setCollator_type(modelCollatorMap.getCollator_type());
								collatorMap.setOutput_message_signature(modelCollatorMap.getOutput_message_signature());
								collatorMap.setMap_inputs(modelCollatorMap.getMap_inputs());
								collatorMap.setMap_outputs(modelCollatorMap.getMap_outputs());

								ProtobufService protoServiceParameterBased = new ProtobufServiceImpl();

								try {
									logger.info("Calling setConf for Parameter based collator with collatorMap {}",
											collatorMap);
									protoServiceParameterBased.setConf(collatorMap);
								} catch (Exception e) {
									logger.error("Exception {} in calling setConf for Parameter based Collator", e);
									logger.error("Collator Map was", collatorMap);
								}
								List<Node> collatorImmediateAncestors = successorNode.getImmediateAncestors();

								Map<String, Object> paramCollateInput = new HashMap<String, Object>();
								for (Node n : collatorImmediateAncestors)

								{
									paramCollateInput.put(n.getContainerName(), n.getNodeOutput());

								}

								// set some dummy output for Collator. It has
								// already given out the correct output to its
								// children.

								// call collate Data
								try {
									collatorOutput = protoServiceParameterBased
											.parameterBasedCollateData(paramCollateInput);
								} catch (Exception e) {
									logger.error("Exception in calling parameterBasedCollateData {}", e);
									logger.error("Input Map was {}", paramCollateInput);

								}
								// set output for Collator
								successorNode.setNodeOutput(collatorOutput);

								// set output available for Collator
								successorNode.setOutputAvailable(true);
							}

							// call probe if required.

							NewThreadAttributes newThreadAttributes = new NewThreadAttributes();
							// set the all the required attributes.

							newThreadAttributes.setpNode(successorNode);
							newThreadAttributes.setsNode(null);
							newThreadAttributes.setOut(collatorOutput);
							newThreadAttributes.setId(1);
							newThreadAttributes.setPbNode(probeNode);

							// create a thread with the newThreadAttributes
							// objects
							logger.info("Starting a new thread with Node {} as predecessor",
									successorNode.getContainerName());
							service4.execute(new NewModelCaller(newThreadAttributes));

							if (finalOutput == null) {
								continue;
							} else
								break;

						}

						// Normal node i.e. ML model case
						else {
							try {
								// call contact node.
								String url = constructURL(successorNode);
								logger.info("Thread {} - Contacting node {}", Thread.currentThread().getId(),
										successorNode.getContainerName());
								byte[] normalNodeOutput = contactnode(predecessorNode.getNodeOutput(), url,
										successorNode.getContainerName());

								// set the output for successorNode
								successorNode.setNodeOutput(normalNodeOutput);

								// set outputAvailable for successorNode
								successorNode.setOutputAvailable(true);

								// IF THIS IS NOT THE LAST NODE, call
								// traverseEachNode on a separate thread.
								ArrayList<ConnectedTo> connectedToListofSuccessorNode = findconnectedto(successorNode,
										successorNode.getOperationSignatureList().get(0).getOperationSignature()
												.getOperationName());
								if (!connectedToListofSuccessorNode.isEmpty()) {
									// traverseEachNode(successorNode, null,
									// successorNode.getNodeOutput(), 1);

									NewThreadAttributes newThreadAttributes = new NewThreadAttributes();
									// set the all the required attributes.

									newThreadAttributes.setpNode(successorNode);
									newThreadAttributes.setsNode(null);
									newThreadAttributes.setOut(successorNode.getNodeOutput());
									newThreadAttributes.setId(1);
									newThreadAttributes.setPbNode(probeNode);

									// create a thread with the
									// newThreadAttributes
									// objects
									logger.info("Starting a new thread with Node {} as predecessor",
											successorNode.getContainerName());

									service4.execute(new NewModelCaller(newThreadAttributes));

								} else {

									// contact probe for input and output
									// messages for the last node.

									/*
									 * if (probePresent == true) {
									 * 
									 * String probeUrl =
									 * constructURL(probeNode);
									 * contactProbe(successorNode.
									 * getImmediateAncestors().get(0).
									 * getNodeOutput(), probeUrl,
									 * probeNode.getContainerName(),
									 * successorNode.getOperationSignatureList()
									 * .get(0).getOperationSignature().
									 * getInputMessageName(), successorNode); }
									 * 
									 * if (probePresent == true) {
									 * 
									 * String probeUrl =
									 * constructURL(probeNode);
									 * contactProbe(successorNode.getNodeOutput(
									 * ), probeUrl,
									 * probeNode.getContainerName(),
									 * successorNode.getOperationSignatureList()
									 * .get(0).getOperationSignature().
									 * getInputMessageName(), successorNode); }
									 */

									finalOutput = successorNode.getNodeOutput();
									logger.info("Thread {} RETURNING FINAL OUTPUT {} FROM LAST NODE",
											Thread.currentThread().getId(), finalOutput);
									service4.shutdown();
								}

								// call probe if required.
								/*
								 * if (probePresent == true) {
								 * 
								 * String probeUrl = constructURL(probeNode);
								 * contactProbe(successorNode.
								 * getImmediateAncestors().get(0).getNodeOutput(
								 * ), probeUrl, probeNode.getContainerName(),
								 * successorNode.getOperationSignatureList()
								 * .get(0).getOperationSignature().
								 * getInputMessageName(), successorNode); }
								 */

								if (finalOutput == null) {
									continue;
								} else
									break;

							} catch (Exception e) {
								/*
								 * return (ResponseEntity<T>) new
								 * ResponseEntity<>( "Exception for" + " " +
								 * inpContainer + ":::" + e.getMessage(),
								 * HttpStatus.INTERNAL_SERVER_ERROR);
								 */

							}

						} // normal node ends

					} // immediate Ancestors output available checking loop.
						// Else waste time here and re-check.

				} // matrix location integer checking ends.

			} // inner for loop ends

			// currentThreadHasProcessedAllModels

			if (finalOutput == null) {
				continue;
			} else
				break;

		} // outer for loop ends
		logger.info("Thread {} reporting from outer TRAVERSENODE ENDS ", Thread.currentThread().getId());

	}

	private String constructURL(Node n) {

		String finalUrl = "";
		Blueprint blueprint = TaskManager.getBlueprint();
		DockerInfoList dockerList = TaskManager.getDockerList();

		DockerInfo dInfo = dockerList.findDockerInfoByContainer(n.getContainerName());
		String op = n.getOperationSignatureList().get(0).getOperationSignature().getOperationName();

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(dInfo.getIpAddress()).setPort(new Integer(dInfo.getPort()).intValue())
				.setPath("/" + op);

		try {
			finalUrl = builder.build().toURL().toString();
		} catch (MalformedURLException | URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return finalUrl;
	}

}
