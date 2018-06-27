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
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.acumos.bporchestrator.collator.service.ProtobufService;
import org.acumos.bporchestrator.collator.service.ProtobufServiceImpl;
import org.acumos.bporchestrator.collator.vo.Configuration;
import org.acumos.bporchestrator.model.*;
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
	private ProtobufService protoService;

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

		byte[] finalresults = null;

		Blueprint blueprint = TaskManager.getBlueprint();
		DockerInfoList dockerList = TaskManager.getDockerList();

		// clear the connecttimeout models list
		if (TaskManager.getListofsocketimoutmodels() != null && !TaskManager.getListofsocketimoutmodels().isEmpty()) {
			List list = TaskManager.getListofsocketimoutmodels();
			list.clear();
			TaskManager.setListofsocketimoutmodels(list);
		}

		// clear the Final results
		if (TaskManager.getFinalResults() != null && (TaskManager.getFinalResults().getMsgname() != null)) {
			FinalResults f0 = TaskManager.getFinalResults();
			f0.setMsgname("Begin");
			f0.setFinalresults(null);
			TaskManager.setFinalResults(f0);
		}
		// Probe related.
		boolean probePresent = false;

		String probeContName = "Probe";
		String probeOperation = "data";
		String probeurl = null;

		// Input node related
		String inpcontainer = null;
		String inpoperation = null;

		// Bad execution message
		String badexecutionmsg = null;

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
				return (ResponseEntity<T>) new ResponseEntity<>(finalresults, HttpStatus.PARTIAL_CONTENT);

			}
			if (dockerList == null) {
				logger.error("notify: Need Docker Information... Exiting");
				return (ResponseEntity<T>) new ResponseEntity<>(finalresults, HttpStatus.PARTIAL_CONTENT);
			}

			List<InputPort> inps = blueprint.getInputPorts();
			byte[] output = null;

			// Check if Probe is present in the composite solution. If yes, get
			// its
			// container name, and operation. THIS PROBE DATA WILL BE USED FOR
			// THE DATA
			// SOURCE CASE ONLY.

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
					logger.error("notify: Cannot find docker info about the probe {}", probeContName);
					return (ResponseEntity<T>) new ResponseEntity<>(finalresults, HttpStatus.INTERNAL_SERVER_ERROR);
				}

				URIBuilder builder = new URIBuilder();
				builder.setScheme("http").setHost(d2.getIpAddress()).setPort(new Integer(d2.getPort()).intValue())
						.setPath("/" + probeOperation);

				probeurl = builder.build().toURL().toString();
			}

			// Now call the input node and its operation signature.
			for (InputPort inport : inps) {
				if (inport.getOperationSignature().getOperationName().equals(operation)) {
					inpcontainer = inport.getContainerName();
					inpoperation = operation;
					break;

				}
			}

			String inpmsgname = null;

			Node inpnode = blueprint.getNodebyContainer(inpcontainer);
			ArrayList<OperationSignatureList> inplosl = inpnode.getOperationSignatureList();
			for (OperationSignatureList inposl : inplosl) {
				inpmsgname = inposl.getOperationSignature().getInputMessageName();
				break;
			}

			DockerInfo d2 = dockerList.findDockerInfoByContainer(inpcontainer);

			if (d2 == null) { // what to do if the deployer passed
				// incomplete docker info ???
				logger.error("notify: Cannot find docker info about the dontainer {}", inpcontainer);
				return (ResponseEntity<T>) new ResponseEntity<>(finalresults, HttpStatus.INTERNAL_SERVER_ERROR);
			}

			URIBuilder builder = new URIBuilder();
			builder.setScheme("http").setHost(d2.getIpAddress()).setPort(new Integer(d2.getPort()).intValue())
					.setPath("/" + inpoperation);

			boolean singlemodel = false;

			// Check if the input node has any dependencies? if no, set
			// singlemodel to true.
			ArrayList<ConnectedTo> inpnodedeps = findconnectedto(inpnode, inpoperation);
			if (inpnodedeps == null || inpnodedeps.isEmpty()) {
				singlemodel = true;
			}

			String inpurl = builder.build().toURL().toString();
			try {
				output = contactnode(binaryStream, inpurl, inpcontainer);
			} catch (SocketTimeoutException ste) {
				return (ResponseEntity<T>) new ResponseEntity<>(
						"Connection request timeout for" + " " + inpcontainer + ":::" + ste.getMessage(),
						HttpStatus.INTERNAL_SERVER_ERROR);

			} catch (IOException ioe) {
				return (ResponseEntity<T>) new ResponseEntity<>(
						"IO Exception for" + " " + inpcontainer + ":::" + ioe.getMessage(),
						HttpStatus.INTERNAL_SERVER_ERROR);

			} catch (Exception e) {
				return (ResponseEntity<T>) new ResponseEntity<>(
						"Exception for" + " " + inpcontainer + ":::" + e.getMessage(),
						HttpStatus.INTERNAL_SERVER_ERROR);

			}
			/*****************/

			// If output of the first model is null, stop execution and return
			// to the data source.

			if (output == null || (output.length == 0)) {
				logger.error("The output of {} is null. Model connector will not proceed.", inpcontainer);

				FinalResults f = new FinalResults();
				f.setMsgname("Error from container " + inpcontainer);
				f.setFinalresults(output);
				TaskManager.setFinalResults(f);

				badexecutionmsg = f.getMsgname();
				return (ResponseEntity<T>) new ResponseEntity<>(badexecutionmsg, HttpStatus.INTERNAL_SERVER_ERROR);

			}
			/*****************/

			/*
			 * if probe in composite solution send the input message of input
			 * node to probe also and wait for http }
			 */
			if (probePresent == true) {

				// create a dummydepsofdeps null object
				ArrayList<ConnectedTo> dummydepsofdeps = null;
				prepareAndContactprobe(inpoperation, probeurl, inpcontainer, binaryStream, output, dummydepsofdeps,
						singlemodel);

			}

			// If it is a single model, then return the correct output of the
			// single model.
			if (singlemodel != true) {
				notifyNextNode(output, inpnode, inpoperation, probePresent, probeContName, probeOperation, probeurl);
			} else {
				return (ResponseEntity<T>) new ResponseEntity<>(output, HttpStatus.OK);
			}
		} // try ends for notify method's chunk
		catch (SocketTimeoutException ste) {
			return (ResponseEntity<T>) new ResponseEntity<>(ste.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (IOException ioe) {
			return (ResponseEntity<T>) new ResponseEntity<>(ioe.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (Exception ex) {

			// Return error due to exception in notifyNextnode
			FinalResults f1 = TaskManager.getFinalResults();
			finalresults = f1.getFinalresults();
			badexecutionmsg = f1.getMsgname();
			logger.error("notify: Notify failed {}", ex);
			return (ResponseEntity<T>) new ResponseEntity<>(badexecutionmsg, HttpStatus.INTERNAL_SERVER_ERROR);

		}

		// if no exceptions in the notifyNextNode

		FinalResults f2 = TaskManager.getFinalResults();
		finalresults = f2.getFinalresults();
		badexecutionmsg = f2.getMsgname();
		if ((badexecutionmsg).contains("Error")) {
			// Return error due to correct execution of notifyNextNode but
			// output null due to wrong model behaviours
			return (ResponseEntity<T>) new ResponseEntity<>(badexecutionmsg, HttpStatus.INTERNAL_SERVER_ERROR);
		} else {
			return (ResponseEntity<T>) new ResponseEntity<>(finalresults, HttpStatus.OK);
		}
	}

	/**
	 * @param output
	 *            The result of the previous node
	 * @param n
	 *            Current node
	 * @param oprn
	 *            Current node's operation which was called
	 * @param prbpresent
	 *            Indicator of probe presence in the blueprint
	 * @param prbcontainername
	 *            Probe's container name if Probe is present
	 * @param prboperation
	 *            Probe's operation name if Probe is present
	 * @param prburl
	 *            Probe's url if Probe is present
	 * @return byte[] output stream
	 * @throws Exception
	 *             : Exception
	 */
	public byte[] notifyNextNode(byte[] output, Node n, String oprn, boolean prbpresent, String prbcontainername,
			String prboperation, String prburl) throws Exception {

		Blueprint blueprint = TaskManager.getBlueprint();
		DockerInfoList dockerList = TaskManager.getDockerList();

		// Find dependents of the node. If the dependent is null, then send the
		// output
		// message to the probe and stop looping.
		ArrayList<ConnectedTo> deps = null;
		ArrayList<ConnectedTo> depsofdeps = null;
		try {
			deps = findconnectedto(n, oprn);
		} catch (Exception e) {
			logger.error("notifyNextNode: Finding Connected to failed {}", e);
		}

		// First time the loop executes (i.e when we call 1st dependent node,
		// the value
		// of depoutput is output from previous databroker or input node.
		// Later on on every call it will be the output of the contactnode call
		// passed
		// as depoutput2)
		byte[] depoutput = output;
		byte[] depoutput2 = null;

		// for loop here to loop through the dependents.
		for (ConnectedTo depitem : deps) {

			String depcontainername = depitem.getContainerName();
			String depoperation = depitem.getOperationSignature().getOperationName();
			Node depnode = blueprint.getNodebyContainer(depcontainername);

			// Find dependent's url and name for the node. Url finding will also
			// need the
			// dependent's operation
			DockerInfo d3 = dockerList.findDockerInfoByContainer(depcontainername);

			if (d3 == null && !(depcontainername.contains("Splitter") || depcontainername.contains("Collator"))) {
				// what to do if the deployer passed incomplete dockerInfo ???
				logger.error("Cannot find docker info about the container {}", depcontainername);
			}

			// Contact the dependent node.
			if (!(((depnode.getNodeType()).equals("Splitter")) || ((depnode.getNodeType()).equals("Collator")))) {

				URIBuilder builder = new URIBuilder();
				builder.setScheme("http").setHost(d3.getIpAddress()).setPort(new Integer(d3.getPort()).intValue())
						.setPath("/" + depoperation);

				String depurl = builder.build().toURL().toString();

				try {
					depoutput2 = contactnode(depoutput, depurl, depcontainername);
				} catch (SocketTimeoutException ex) {
					logger.error("SocketTimeoutException in notifyNextNode() method:" + ex + " while calling"
							+ depcontainername);
					ex.printStackTrace();
					throw new SocketTimeoutException("Connection timed out for " + depcontainername);

				} catch (IOException ex) {
					logger.error("IOException in notifyNextNode method while calling a dependent node: " + ex
							+ " while calling" + depcontainername);
					ex.printStackTrace();

					throw ex;

				} catch (Exception ex) {
					logger.error("Exception in notifyNextNode method while calling a dependent node: " + ex
							+ " while calling" + depcontainername);
					ex.printStackTrace();
					throw ex;

				}

				// If output of any model is null, Model connector stops
				// immediately and returns.
				if (depoutput2 == null || (depoutput2.length == 0)) {
					logger.error("The output of {} is null. Model connector will not proceed.", depcontainername);

					FinalResults f4 = new FinalResults();
					f4.setMsgname("Error from container " + depcontainername);
					f4.setFinalresults(depoutput2);
					TaskManager.setFinalResults(f4);
					return depoutput2;

				}

			}

			// find dependents of the current dependent to see if we need to
			// send input msg
			// or both the input and output msg.
			try {
				depsofdeps = findconnectedto(depnode, depoperation);

				if ((depnode.getNodeType()).equals("Splitter")) {
					List<byte[]> mergedoutput;

					// CALLING MULTIPLE MODELS WITH SPLITTER INPUT i.e
					// SPLITTER's PRECEDING NODE's
					// OUTPUT.
					// THIS NEEDS TO CHANGE IN PARAMETER BASED SPLITTER WHERE
					// ITS INPUT AND OUTPUT
					// WILL NOT BE THE SAME.

					// CALL MULTIPLE MODELS SEQUENTIALLY
					// mergedoutput = callMultipleModels(depoutput, depsofdeps,
					// prburl,prbpresent);

					// CALL MULTIPLE MODELS PARALLELY
					mergedoutput = callMultipleModelsParallelyClient(depoutput, depsofdeps, prburl, prbpresent);

					// Stop execution if any of the PARALLEL models had sent
					// null output or not responded.
					FinalResults f5 = TaskManager.getFinalResults();
					if (f5 != null && f5.getMsgname() != null) {
						String executionstatusmsg = f5.getMsgname();
						if ((executionstatusmsg.contains("Error"))) {

							// Return from here.
							return null;
						}
					}
					// whether splitter has collator reference or not
					depsofdeps = linkRelatedCollator(depsofdeps);

					// configure collator here
					String collatorcontainer = depsofdeps.get(0).getContainerName();
					Node collatornode = blueprint.getNodebyContainer(collatorcontainer);
					String containeroperation = collatornode.getOperationSignatureList().get(0).getOperationSignature()
							.getOperationName();
					String collatortype = collatornode.getCollatorMap().getCollatorType();
					String protobufFileStr = collatornode.getCollatorMap().getOutputMessageSignature();
					logger.info("set all the configuration related values for the collator is started");
					Configuration configuration = new Configuration();
					logger.info("collatortype" + collatortype);
					configuration.setCollator_type(collatortype);
					logger.info("protobufFileStr" + protobufFileStr);

					configuration.setProtobufFileStr(protobufFileStr);
					protoService.setConf(configuration);
					try {
						protoService.processProtobuf();
					} catch (Exception ex) {
						logger.error("Exception in processProtobuf()", ex);
					}

					logger.info("set all the configuration related values for the collator is ended");

					// call collator
					// ProtobufService protobufService1 = new
					// ProtobufServiceImpl();

					// REAL CALL
					logger.info("Call the protoService.collateData api");

					try {
						if (null != mergedoutput && !mergedoutput.isEmpty()) {
							logger.info("collate data input" + mergedoutput);
							depoutput2 = protoService.collateData(mergedoutput);
							logger.info("collate data output" + depoutput2);
						} else {
							logger.debug("Please check final combine result from multiple models are working or not");
						}

					} catch (Exception ex) {
						logger.error("calling collateData method of the Collator failed{}", ex);
					}

					// SIMULATED CALL
					// depoutput2 = httpPost("http://127.0.0.1:9222/collate",
					// output);

					// Set depnode to collator node before call notifyNextNode
					// recursively
					depnode = collatornode;
					depoperation = containeroperation;
					depcontainername = collatorcontainer;

				}

			}

			catch (Exception e1) {
				logger.error("notifyNextNode failed {}", e1);
				throw e1;
			}

			if (prbpresent == true
					&& !(((depcontainername.contains("Splitter")) || (depcontainername.contains("Collator"))))) {

				// For any node (if probe is present), contact probe with input
				// message
				// For the last node(if probe is present), contact probe with
				// input message name
				// & contact probe with output message name

				prepareAndContactprobe(depoperation, prburl, depcontainername, depoutput, depoutput2, depsofdeps,
						false);

			}

			if (depsofdeps.isEmpty()) {
				FinalResults f3 = new FinalResults();
				// Set the correct output. Set the msg to be success.
				// The output is the output of the composite solution upon
				// correct execution.
				f3.setMsgname("success");
				f3.setFinalresults(depoutput2);
				TaskManager.setFinalResults(f3);
			}

			// Call notifyNextNode function
			if (!(depsofdeps.isEmpty())) {
				byte[] mdoutput = notifyNextNode(depoutput2, depnode, depoperation, prbpresent, prbcontainername,
						prboperation, prburl);
				if (mdoutput == null || mdoutput.length == 0) {
					return null;
				}

			}
		}
		return output;
	}

	/**
	 * Contacting probe
	 *
	 * @param probeurl
	 *            : url of the probe
	 * @param binaryStream
	 *            : input protobuf binary message.
	 * @param probeurl
	 *            : url of probe
	 * @param msg_name
	 *            : input message name
	 * @param n
	 *            : the node object
	 * @param pb_c_name
	 *            : probe container name
	 *
	 * @return : returns byte[] type
	 * @throws IOException
	 *             : IO exception
	 */

	public byte[] contactProbe(byte[] binaryStream, String probeurl, String pb_c_name, String msg_name, Node n)
			throws Exception {

		Blueprint blueprint = TaskManager.getBlueprint();
		DockerInfoList dockerList = TaskManager.getDockerList();

		byte[] output2;
		Node probeNode = blueprint.getNodebyContainer(pb_c_name);
		logger.info("contactProbe: Thread {} : Notifying probe: {}, POST: {}, proto uri: {} , message Name: {}",
				Thread.currentThread().getId(), pb_c_name, probeurl, n.getProtoUri(), msg_name);

		output2 = httpPost(probeurl, binaryStream, n.getProtoUri(), msg_name);
		return output2;
	}

	void prepareAndContactprobe(String depoperation, String prburl, String depcontainername, byte[] depoutput,
			byte[] depoutput2, ArrayList<ConnectedTo> depsofdeps, boolean singlemodel) {

		// Call notifyprobe if indicator is true.
		// Message name needed for sending to probe. (Find the input message
		// name for
		// the node. This will be needed later if probe is present.)

		Blueprint blueprint = TaskManager.getBlueprint();
		DockerInfoList dockerInfoList = TaskManager.getDockerList();
		String depinpmsgname = null;
		String depoutmsgname = null;
		byte[] probeout = null;
		byte[] probeout2 = null;
		Node depnode = blueprint.getNodebyContainer(depcontainername);
		ArrayList<OperationSignatureList> deplosl = depnode.getOperationSignatureList();
		for (OperationSignatureList deposl : deplosl) {
			// checking if the operation is the same as requested operation.
			// Only then
			// proceed to set the depinpmsgname and depoutmsgname accordingly.
			if ((deposl.getOperationSignature().getOperationName()).equals(depoperation))

			{
				depinpmsgname = deposl.getOperationSignature().getInputMessageName();
				depoutmsgname = deposl.getOperationSignature().getOutputMessageName();
				break;
			}

		}

		/*
		 * If probe is present- For first node (with dummydepsofdeps = null) or
		 * any node with depsofdeps not empty, contact probe with input message
		 */

		if (((depsofdeps == null) || !(depsofdeps.isEmpty())) && singlemodel == false) {
			try {
				logger.info("notifyNextNode: Thread {} : Notifying PROBE for node name: {}, inp msg name: {} , msg: {}",
						Thread.currentThread().getId(), depcontainername, depinpmsgname,
						(Arrays.toString(depoutput)).substring(0, 20));

				probeout = contactProbe(depoutput, prburl, depcontainername, depinpmsgname, depnode);
			} catch (Exception e) {
				logger.error("Contacting probe failed {}", e);
			}
		}

		// If probe is present -For the last node contact probe with input
		// message name
		// & contact probe with output message name
		if (((depsofdeps != null) && (depsofdeps.isEmpty())) || (singlemodel == true)) {

			try {

				logger.info("notifyNextNode: Thread {} : Notifying PROBE for node name: {}, inp msg name: {} , msg: {}",
						Thread.currentThread().getId(), depcontainername, depinpmsgname,
						(Arrays.toString(depoutput)).substring(0, 20));

				probeout = contactProbe(depoutput, prburl, depcontainername, depinpmsgname, depnode);
			} catch (Exception e) {
				logger.error("notifyNextNode: Contacting probe failed {}", e);
			}
			try {
				// Note here depoutput2 is assigned to local depoutputof this
				// function.
				logger.info("notifyNextNode: Thread {} : Notifying PROBE for node name: {}, out msg name: {} , msg: {}",
						Thread.currentThread().getId(), depcontainername, depoutmsgname,
						(Arrays.toString(depoutput2)).substring(0, 20));

				probeout2 = contactProbe(depoutput2, prburl, depcontainername, depoutmsgname, depnode);
			} catch (Exception e) {
				logger.error("notifyNextNode: Contacting probe failed - ", e);
			}

		}

	}

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
			logger.error("contactnode: Contacting node failed - ", e);
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

		logger.info("putBluePrint: Returning HttpStatus.OK from putBlueprint");
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
				ExecutorService service = Executors.newFixedThreadPool(1);
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
						service.execute(new DBResponseRunnable(mcAttributes));
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

	// To Call models connected to the splitter sequentially
	private List<byte[]> callMultipleModels(byte[] depoutput, ArrayList<ConnectedTo> depsofdeps, String prburl,
			boolean probePresent) throws Exception {
		List<byte[]> listOfbyte = new ArrayList<byte[]>();
		DockerInfoList dockerInfoList = TaskManager.getDockerList();
		try {
			if (null != dockerInfoList) {
				for (ConnectedTo connectedNode : depsofdeps) {
					String depcontainername = connectedNode.getContainerName();
					DockerInfo dockerInfo = dockerInfoList.findDockerInfoByContainer(depcontainername);

					// Call the models/connectedNode
					byte[] modelOutput = contactnode(depoutput,
							"http://" + dockerInfo.getIpAddress() + ":" + dockerInfo.getPort() + "/"
									+ connectedNode.getOperationSignature().getOperationName(),
							dockerInfo.getContainer());
					String depoperation = connectedNode.getOperationSignature().getOperationName();

					// Contact the probe for the model /connectedNode
					if (probePresent == true) {
						prepareAndContactprobe(depoperation, prburl, depcontainername, depoutput, modelOutput,
								depsofdeps, false);
					}

					// call to probe ends
					listOfbyte.add(modelOutput);
				}
			}

		} catch (Exception ex) {
			logger.error("Exception in callMultipleModels() method :" + ex);
			throw ex;
		}

		return listOfbyte;
	}

	// Client To Call models connected to the splitter parallely
	private <T> List<T> callMultipleModelsParallelyClient(byte[] depoutput, ArrayList<ConnectedTo> depsofdeps,
			String prburl, boolean probePresent) throws Exception {
		List<byte[]> listOfbyte = new ArrayList<byte[]>();
		try {
			ExecutorService executor = Executors.newFixedThreadPool(4);

			List<Future<Map<String, byte[]>>> listOfMlOutput = new ArrayList<Future<Map<String, byte[]>>>();

			for (ConnectedTo connectedNode : depsofdeps) {
				String depcontainername = connectedNode.getContainerName();
				String depoperation = connectedNode.getOperationSignature().getOperationName();
				Callable<Map<String, byte[]>> callable = new CallMultipleModelParallely(depoutput, depcontainername,
						depoperation, prburl, depsofdeps, probePresent);
				Future<Map<String, byte[]>> future = executor.submit(callable);

				listOfMlOutput.add(future);

			}
			for (Future<Map<String, byte[]>> fut : listOfMlOutput) {
				String modelname = "";
				try {

					// Map has been added to capture the results of models to
					// which connection timed out. i.e SockerTimoutException.
					Map mapinsideafuture = fut.get();

					Set mapSet = (Set) mapinsideafuture.entrySet();
					Iterator mapIterator = mapSet.iterator();

					Map.Entry mapEntry = (Map.Entry) mapIterator.next();
					// getKey Method of HashMap access a key of map. This
					// key is the model name for which we are receiving
					// output.
					modelname = (String) mapEntry.getKey();

					// get the byte[] output for this key from the hashmap
					byte[] op = (byte[]) mapinsideafuture.get(modelname);

					// Return if any of the PARALLEL models sent null output.
					if (op == null || (op.length == 0)) {

						logger.error("The output of {} is null. Model connector will not proceed.", modelname);
						FinalResults f4 = new FinalResults();
						f4.setMsgname("Error from container " + modelname);
						f4.setFinalresults(op);
						TaskManager.setFinalResults(f4);

						// immediately return
						return null;

					} else

					{
						listOfbyte.add(op);
					}
				} catch (Exception ex) {

					if (ex.getMessage().contains("SocketTimeoutException")) {
						TaskManager.getListofsocketimoutmodels();
						logger.error("SocketTimeoutException in callMultipleModelsParallelyClient() method:" + ex);

						ex.printStackTrace();
						throw new SocketTimeoutException(
								"Connection timed out for " + TaskManager.getListofsocketimoutmodels());
					} else {
						logger.error(
								"Exception happens while fetching the thread return result in callMultipleModelsParallelyClient() method:"
										+ ex);
						ex.printStackTrace();
						throw ex;
					}
				}

			}
		}

		catch (Exception ex) {
			logger.error("Exception in callMultipleModelsParallelyClient() method:" + ex);
			throw ex;
		}

		return (List<T>) listOfbyte;
	}

	private ArrayList<ConnectedTo> linkRelatedCollator(ArrayList<ConnectedTo> connectedNodeList) throws Exception {
		try {
			Blueprint blueprint = TaskManager.getBlueprint();
			ArrayList<ConnectedTo> connectedTo = null;
			for (ConnectedTo connectedNode : connectedNodeList) {
				String depcontainername = connectedNode.getContainerName();
				Node depnode = blueprint.getNodebyContainer(depcontainername);
				ArrayList<OperationSignatureList> listOfOperationSigList = depnode.getOperationSignatureList();
				if (null != listOfOperationSigList && !listOfOperationSigList.isEmpty()) {
					connectedTo = listOfOperationSigList.get(0).getConnectedTo();
				}
				String container_name = connectedTo.get(0).getContainerName();
				if (container_name.contains("Collator")) {
					return connectedTo;
				} else {
					return null;
				}
			}

		} catch (Exception ex) {
			logger.error("Exception in  linkRelatedCollator() method:" + ex);
			throw new Exception();
		}

		return null;
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

}