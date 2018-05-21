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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.acumos.bporchestrator.controller.DSAsyncResponseRunnable;
import org.acumos.bporchestrator.controller.DBResponseRunnable;
import org.acumos.bporchestrator.MCAttributes;
import org.acumos.bporchestrator.model.*;
import org.acumos.bporchestrator.util.TaskManager;
import org.apache.commons.io.IOUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.acumos.bporchestrator.controller.FinalResults;

/**
 * Rest Controller that handles the API end points
 */
@RestController
public class BlueprintOrchestratorController {

	private static final Logger logger = LoggerFactory.getLogger(BlueprintOrchestratorController.class);

	/**
	 * 
	 * @param binaryStream
	 *            This is the binary stream from the data source
	 * @param operation
	 *            This specifies the input_operation_signature of the runtime
	 *            orchestrator should invoke on the first node based on
	 *            blueprint.json
	 * @return Byte stream
	 */

	// The MC is triggerred by a /{operation} request. The requester also sends the
	// protobuf binary message.

	@ApiOperation(value = "operation on the first node in the chain", response = byte.class, responseContainer = "Page")
	@RequestMapping(path = "/{operation}", method = RequestMethod.POST)
	public ResponseEntity<byte[]> notify(
			@ApiParam(value = "Inital request to start deploying... This binary stream is in protobuf format.", required = false) @Valid @RequestBody byte[] binaryStream,
			@ApiParam(value = "This operation should match with one of the input operation signatures in blueprint.json", required = true) @PathVariable("operation") String operation) {

		byte[] finalresults = null;

		Blueprint blueprint = TaskManager.getBlueprint();
		DockerInfoList dockerList = TaskManager.getDockerList();
		;

		// Probe related.
		boolean probePresent = false;

		String probeContName = "Probe";
		String probeOperation = "data";
		String probeurl = null;
		ArrayList<OperationSignatureList> ListOfProbeOpSigList = null;

		// Input node related
		String inpcontainer = null;
		String inpoperation = null;

		// On receiving this request, the data source is blocked.
		// We will need to unblock it only when we receive output from the first model.

		// Check if blueprint and dockerList has been populated.
		try {
			logger.info("****************************************************************************");
			logger.info("Receiving /{} request: {}", operation, Arrays.toString(binaryStream));
			if (blueprint == null) {
				logger.error("Empty blueprint JSON");
				return new ResponseEntity<>(finalresults, HttpStatus.PARTIAL_CONTENT);

			}
			if (dockerList == null) {
				logger.error("Need Docker Information... Exiting");
				return new ResponseEntity<>(finalresults, HttpStatus.PARTIAL_CONTENT);
			}

			List<InputPort> inps = blueprint.getInputPorts();
			List<Node> allnodes = blueprint.getNodes();

			String urlBase = null;
			byte[] output = null;

			// Check if Probe is present in the composite solution. If yes, get its
			// container name, and operation. THIS PROBE DATA WILL BE USED FOR THE DATA
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
					logger.error("Cannot find docker info about the probe {}", probeContName);
					return new ResponseEntity<>(finalresults, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				urlBase = "http://" + d2.getIpAddress() + ":" + d2.getPort() + "/";
				probeurl = urlBase + probeOperation;
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
				logger.error("Cannot find docker info about the dontainer {}", inpcontainer);
				return new ResponseEntity<>(finalresults, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			urlBase = "http://" + d2.getIpAddress() + ":" + d2.getPort() + "/";
			String inpurl = urlBase + inpoperation; // Is this always get_image?

			output = contactnode(binaryStream, inpurl, inpcontainer);

			/*
			 * if probe in composite solution send the input message of input node to probe
			 * also and wait for http }
			 */
			if (probePresent == true) {
				logger.info("Notifying PROBE for node name: {}, inp msg name: {} , msg: {}", inpnode.getContainerName(),
						inpmsgname, binaryStream);

				byte[] out = contactProbe(binaryStream, probeurl, probeContName, inpmsgname, inpnode);
			}

			notifynextnode(output, inpnode, inpoperation, probePresent, probeContName, probeOperation, probeurl);
			FinalResults f = TaskManager.getFinalResults();
			finalresults = f.getFinalresults();
			/*
			 * Adding direct call and commenting this for directly informing caller. //
			 * Unblock the data source.
			 * logger.info("Unblocking data source after receiving output from input node."
			 * );
			 * 
			 * if (output != null) { MCAttributes mcAttributes = new MCAttributes(); // set
			 * the all the required attributes. mcAttributes.setOutput(output);
			 * mcAttributes.setCurrentNode(inpnode);
			 * mcAttributes.setCurrentOperation(inpoperation);
			 * mcAttributes.setProbePresent(probePresent);
			 * mcAttributes.setProbeContName(probeContName);
			 * mcAttributes.setProbeOperation(probeOperation);
			 * mcAttributes.setProbeUrl(probeurl); // create a thread with the mcAttribute
			 * objects DSAsyncResponseRunnable dsthread = new
			 * DSAsyncResponseRunnable(mcAttributes); Thread thread = new Thread(dsthread);
			 * // start the thread. thread.start(); return new ResponseEntity<>(results,
			 * HttpStatus.OK); }
			 */

		} // try ends for notify method's chunk
		catch (Exception ex) {
			logger.error("Notify failed {}", ex);
			return new ResponseEntity<>(finalresults, HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return new ResponseEntity<>(finalresults, HttpStatus.OK);
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
	 */
	public byte[] notifynextnode(byte[] output, Node n, String oprn, boolean prbpresent, String prbcontainername,
			String prboperation, String prburl) throws Exception {

		Blueprint blueprint = TaskManager.getBlueprint();
		DockerInfoList dockerList = TaskManager.getDockerList();

		// Find dependents of the node. If the dependent is null, then send the output
		// message to the probe and stop looping.
		ArrayList<ConnectedTo> deps = null;
		ArrayList<ConnectedTo> depsofdeps = null;
		try {
			deps = findconnectedto(n, oprn);
		} catch (Exception e) {
			logger.error("Finding Connected to failed {}", e);
		}

		// First time the loop executes (i.e when we call 1st dependent node, the value
		// of depoutput is output from previous databroker or input node.
		// Later on on every call it will be the output of the contactnode call passed
		// as depoutput2)
		byte[] depoutput = output;
		byte[] depoutput2 = null;

		// for loop here to loop through the dependents.
		for (ConnectedTo depitem : deps) {

			String depcontainername = depitem.getContainerName();
			String depoperation = depitem.getOperationSignature().getOperationName();
			Node depnode = blueprint.getNodebyContainer(depcontainername);

			// Find dependent's url and name for the node. Url finding will also need the
			// dependent's operation
			DockerInfo d3 = dockerList.findDockerInfoByContainer(depcontainername);

			if (d3 == null) { // what to do if the deployer passed
				// incomplete docker info ???
				logger.error("Cannot find docker info about the probe {}", prbcontainername);
			}
			String depurlBase = "http://" + d3.getIpAddress() + ":" + d3.getPort() + "/";
			String depurl = depurlBase + depoperation;

			// Contact the dependent node.
			depoutput2 = contactnode(depoutput, depurl, depcontainername);

			// Call notifyprobe if indicator is true.
			// Message name needed for sending to probe. (Find the input message name for
			// the node. This will be needed later if probe is present.)
			String depinpmsgname = null;
			String depoutmsgname = null;

			// find dependents of the current dependent to see if we need to send input msg
			// or both the input and output msg.
			try {
				depsofdeps = findconnectedto(depnode, depoperation);
			} catch (Exception e1) {
				logger.error("Finding connected to failed {}", e1);
			}

			if (prbpresent == true) {

				ArrayList<OperationSignatureList> deplosl = depnode.getOperationSignatureList();
				for (OperationSignatureList deposl : deplosl) {
					if ((deposl.getOperationSignature().getOperationName()) == depoperation)
						;
					{
						depinpmsgname = deposl.getOperationSignature().getInputMessageName();
						depoutmsgname = deposl.getOperationSignature().getOutputMessageName();
						break;
					}

				}

				// For any node (if probe is present), contact probe with input message
				if (!(depsofdeps.isEmpty())) {
					try {
						logger.info("Thread {} : Notifying PROBE for node name: {}, inp msg name: {} , msg: {}",
								Thread.currentThread().getId(), depcontainername, depinpmsgname, depoutput);

						byte[] probeout = contactProbe(depoutput, prburl, depcontainername, depinpmsgname, depnode);
					} catch (IOException e) {
						logger.error("Contacting probe failed {}", e);
					}
				}

				// For the last node(if probe is present), contact probe with input message name
				// & contact probe with output message name
				if (depsofdeps.isEmpty() && !(n.getNodeType().equalsIgnoreCase("Probe"))) {

					try {

						logger.info("Thread {} : Notifying PROBE for node name: {}, inp msg name: {} , msg: {}",
								Thread.currentThread().getId(), depcontainername, depinpmsgname, depoutput);

						byte[] probeout = contactProbe(depoutput, prburl, depcontainername, depinpmsgname, depnode);
					} catch (IOException e) {
						logger.error("Contacting probe failed {}", e);
					}
					try {
						logger.info("Thread {} : Notifying PROBE for node name: {}, out msg name: {} , msg: {}",
								Thread.currentThread().getId(), depcontainername, depoutmsgname, depoutput2);

						byte[] probeout2 = contactProbe(depoutput2, prburl, depcontainername, depoutmsgname, depnode);
					} catch (IOException e) {
						logger.error("Contacting probe failed {}", e);
					}

				}
			}

			if (depsofdeps.isEmpty()) {
				FinalResults f2 = new FinalResults(null);
				f2.setFinalresults(depoutput2);
				TaskManager.setFinalResults(f2);
			}

			// Call notifynextnode function
			if (!(depsofdeps.isEmpty())) {
				notifynextnode(depoutput2, depnode, depoperation, prbpresent, prbcontainername, prboperation, prburl);

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
		logger.info("Thread {} : Notifying probe: {}, POST: {}, proto uri: {} , message Name: {}",
				Thread.currentThread().getId(), pb_c_name, probeurl, n.getProtoUri(), msg_name);
		// logger.info("Thread " + Thread.currentThread().getId() + ": " + "Notifying
		// probe " + pb_c_name + " POST: "
		// + probeurl + " proto uri = [" + n.getProtoUri() + "] message Name = [" +
		// "Sample message" + "]");
		output2 = httpPost(probeurl, binaryStream, n.getProtoUri(), msg_name);
		return output2;
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
	 */

	public byte[] contactnode(byte[] binaryStream, String url, String name) {

		logger.info("Thread {} : Notifying node: {}, POST: {}", Thread.currentThread().getId(), name, url);

		byte[] output4 = null;
		try {
			output4 = httpPost(url, binaryStream);
		} catch (Exception e) {
			logger.error("Contacting node {} failed {}", name, e);
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
	 */

	public byte[] contactdataBroker(String db_url, String db_c_name) {

		// Find the databroker script
		Blueprint blueprint = TaskManager.getBlueprint();
		Node dbnode = blueprint.getNodebyContainer(db_c_name);
		String scriptstring = dbnode.getDataBrokerMap().getScript();

		logger.info("Thread {} : Notifying databroker: {}, POST: {}", Thread.currentThread().getId(), db_c_name,
				db_url);

		byte[] output3 = null;
		try {
			output3 = httpGet(db_url, scriptstring);
			logger.error("Thread {} : Output of data broker is {}", Thread.currentThread().getId(), output3);
		} catch (IOException e) {
			logger.error("Contacting databroker failed {}", e);
		}
		return output3;
	}

	/**
	 * Find connectedTo given a Node and its OperationSgnatureList data structure.
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
		return null; // there will never be an occasion where we will return null. Even in the case
						// where no dependents, the Array list of type connectedTo will be returned by
						// the inner return. But in this case, the Arraylist will be empty and have no
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

		String probeContName = "Probe";
		String probeOperation = "data";

		TaskManager.setBlueprint(blueprintReq);
		Blueprint blueprint = TaskManager.getBlueprint();

		logger.info("Returning HttpStatus.OK from putBlueprint");
		Map<String, String> results = new LinkedHashMap<>();
		results.put("status", "ok");
		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	/**
	 * The end point for the deployer to push docker info.
	 * 
	 * @param dockerListReq
	 *            Holds dockerInfo.json, containing IP addresses and port numbers
	 *            about all containers
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

		Blueprint blueprint = TaskManager.getBlueprint();
		DockerInfoList dockerList = TaskManager.getDockerList();

		// Check if blueprint and dockerList has been populated.

		if (blueprint == null) {
			logger.error("Empty blueprint JSON");
			return new ResponseEntity<>(dbresults, HttpStatus.PARTIAL_CONTENT);
		}
		if (dockerList == null) {
			logger.error("Need Docker Information... Exiting");
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

		// Check if data broker is present in the composite solution. If yes, get its
		// container name, and operation.
		for (Node nd : allnodes) {
			if (nd.getNodeType().equalsIgnoreCase("Databroker")) {
				dataBrokerPresent = true;
				// get its container name : to be used later.
				dataBrokerContName = nd.getContainerName();
				// getting its operation name : to be used later ...OR is it always get_image?
				ListOfDataBrokerOpSigList = nd.getOperationSignatureList();
				for (OperationSignatureList dbosl : ListOfDataBrokerOpSigList) {
					dataBrokerOperation = dbosl.getOperationSignature().getOperationName(); // here we are assuming
																							// databroker will have
																							// only 1 operation.
																							// //this can be
																							// changed.
				}
				break;
			}
		}

		String urlBase = null;
		String databrokerurl = null;
		byte[] output = null;

		// Find the url etc. for this data broker.
		if (dataBrokerPresent == true) {
			DockerInfo d1 = dockerList.findDockerInfoByContainer(dataBrokerContName);

			if (d1 == null) { // what to do if the deployer passed
				// incomplete docker info ???
				logger.error("Cannot find docker info about the data broker {}", dataBrokerContName);
				dbresults.put("status", "ok");
				return new ResponseEntity<>(dbresults, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			urlBase = "http://" + d1.getIpAddress() + ":" + d1.getPort() + "/";
			databrokerurl = urlBase + dataBrokerOperation; // Is this always get_image?
		}

		// Check if Probe is present in the composite solution. If yes, get its
		// container name, and operation. THIS PROBE DATA WILL BE USED FOR THE DATA
		// BROKER CASE ONLY. // Checking here again because requests back
		// to back where taking old probe related values when probe related values were
		// made class fields.

		// IN CASE OF DATA SOURCE, call to /putDockerInfo will initialize the incorrect
		// values. Then the
		// notify function will initialize the correct values.

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
				logger.error("Cannot find docker info about the probe {}", probeContName);
				return new ResponseEntity<>(dbresults, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			urlBase = "http://" + d2.getIpAddress() + ":" + d2.getPort() + "/";
			probeurl = urlBase + probeOperation;
		}

		/*
		 * if Databroker in composite solution { make a POST request to data broker
		 * which will send the message. This is your message}
		 */

		Node dbnode = blueprint.getNodebyContainer(dataBrokerContName);
		if (dataBrokerPresent == true) {
			ExecutorService service = Executors.newFixedThreadPool(1);
			// make a POST request to the databroker and receive response
			do {
				output = contactdataBroker(databrokerurl, dataBrokerContName);
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

					// start the thread.
					// thread.start();
				}

			} while ((output != null) && (output.length != 0));

		}
		dbresults.put("status", "ok");
		logger.info("Thread {} : Returning HttpStatus.OK from putDockerInfo", Thread.currentThread().getId());
		return new ResponseEntity<>(dbresults, HttpStatus.OK);
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
	 * Sending HTTP POST request to Probe
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
		if (protoUrl != null)
			con.setRequestProperty("PROTO-URL", protoUrl);
		if (messageName != null)
			con.setRequestProperty("Message-Name", messageName);
		con.setDoOutput(true);
		OutputStream out = con.getOutputStream();
		out.write(binaryStream);
		out.flush();
		out.close();
		logger.info("Thread {}: HTTPS POST Sent {}", Thread.currentThread().getId(), url);
		String responseMessage = con.getResponseMessage();
		logger.info("Thread {}: GOT RESPONSE Message {}", Thread.currentThread().getId(), responseMessage);
		int responseCode = con.getResponseCode();
		logger.info("Thread {}: GOT RESPONSE CODE {}", Thread.currentThread().getId(), responseCode);
		if ((responseCode == HttpURLConnection.HTTP_OK) || (responseCode == HttpURLConnection.HTTP_CREATED)
				|| (responseCode == HttpURLConnection.HTTP_ACCEPTED)
				|| (responseCode == HttpURLConnection.HTTP_ACCEPTED)
				|| (responseCode == HttpURLConnection.HTTP_NOT_AUTHORITATIVE)
				|| (responseCode == HttpURLConnection.HTTP_NO_CONTENT) || (responseCode == HttpURLConnection.HTTP_RESET)
				|| (responseCode == HttpURLConnection.HTTP_PARTIAL)) {
			return IOUtils.toByteArray(con.getInputStream());
		} else {
			logger.error("Thread {}: ERROR:::::::POST request did not work {}", Thread.currentThread().getId(), url);
		}

		return new byte[0];

	}

	/**
	 * Sending HTTP POST request to the Data Broker
	 * 
	 * @param url
	 *            : url to get
	 * @param the_script
	 *            : actual script to be sent to the data broker.
	 * @return : returns byte[] type
	 * @throws IOException
	 *             : IO exception
	 */
	public byte[] httpPost(String url, String the_script) throws IOException {
		URL obj = new URL(url);

		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setDoOutput(true);
		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		out.writeBytes(the_script);
		out.flush();
		out.close();

		logger.info("Thread {}: HTTPS POST Sent {}", Thread.currentThread().getId(), url);
		String responseMessage = con.getResponseMessage();
		logger.info("Thread {}: GOT RESPONSE Message {}", Thread.currentThread().getId(), responseMessage);
		int responseCode = con.getResponseCode();
		logger.info("Thread {}: GOT RESPONSE CODE {}", Thread.currentThread().getId(), responseCode);
		if ((responseCode == HttpURLConnection.HTTP_OK) || (responseCode == HttpURLConnection.HTTP_CREATED)
				|| (responseCode == HttpURLConnection.HTTP_ACCEPTED)
				|| (responseCode == HttpURLConnection.HTTP_ACCEPTED)
				|| (responseCode == HttpURLConnection.HTTP_NOT_AUTHORITATIVE)
				|| (responseCode == HttpURLConnection.HTTP_NO_CONTENT) || (responseCode == HttpURLConnection.HTTP_RESET)
				|| (responseCode == HttpURLConnection.HTTP_PARTIAL)) {
			return IOUtils.toByteArray(con.getInputStream());
		} else {
			logger.error("Thread {}: ERROR:::::::POST request did not work {}", Thread.currentThread().getId(), url);
		}

		return new byte[0];
	}
	
	/*
	 * 
	 */
	private byte[] httpGet(String url, String the_script) throws IOException {
		url = url + "?" + the_script;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		byte[] output = null;
		con.setRequestMethod("GET");
		// con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());
			output = response.toString().getBytes();
			return output;
		} else {
			System.out.println("GET request not worked");
		}

		return new byte[0];
	}

}
