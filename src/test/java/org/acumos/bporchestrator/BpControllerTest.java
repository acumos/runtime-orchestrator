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

package org.acumos.bporchestrator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.acumos.bporchestrator.controller.DBResponseRunnable;
import org.acumos.bporchestrator.controller.NewModelCaller;
import org.acumos.bporchestrator.model.Blueprint;
import org.acumos.bporchestrator.model.ConnectedTo;
import org.acumos.bporchestrator.model.DataBroker;
import org.acumos.bporchestrator.model.DockerInfo;
import org.acumos.bporchestrator.model.DockerInfoList;
import org.acumos.bporchestrator.model.InputPort;
import org.acumos.bporchestrator.model.MlModel;
import org.acumos.bporchestrator.model.Node;
import org.acumos.bporchestrator.model.OperationSignature;
import org.acumos.bporchestrator.model.OperationSignatureList;
import org.acumos.bporchestrator.model.Orchestrator;
import org.acumos.bporchestrator.model.ProbeIndicator;
import org.acumos.bporchestrator.model.TrainingClient;
import org.acumos.bporchestrator.splittercollator.util.ProtobufUtil;
import org.acumos.bporchestrator.util.DBThreadAttributes;
import org.acumos.bporchestrator.util.NewThreadAttributes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.web.client.HttpStatusCodeException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class BpControllerTest extends AbstractControllerTest {
	private static Logger logger = LoggerFactory.getLogger(BpControllerTest.class);

	@Test
	public void bpControllerTest1() throws Exception {
		try {

			// Test /putBlueprint PUT end point
			logger.info("Testing /putBlueprint PUT method");
			Blueprint bp = new Blueprint();
			bp.setName("Runtime Orchestrator");
			bp.setVersion("1.0.0");

			// Creating Input Port list with 1 input port and add to the
			// blueprint

			InputPort inp1 = new InputPort();

			OperationSignature ios1 = new OperationSignature();
			ios1.setOperationName("classify");

			inp1.setContainerName("image_classifier1");
			inp1.setOperationSignature(ios1);

			List<InputPort> inputportslist = new ArrayList<>();
			inputportslist.add(inp1);

			bp.setInputPorts(inputportslist);

			// Creating node1 and add to the blueprint
			Node node1 = new Node();

			node1.setContainerName("image_classifier1");
			node1.setNodeType("MLModel");
			node1.setImage("someAI-nexus01:8001/image_classifier:1");
			node1.setProtoUri("www.somewhere.com/protourilink");

			OperationSignatureList test_osl1 = new OperationSignatureList();
			OperationSignature test_os_a = new OperationSignature();

			test_os_a.setOperationName("classify");
			test_os_a.setInputMessageName("someinputmsg");
			test_os_a.setOutputMessageName("someoutputmsg");

			test_osl1.setOperationSignature(test_os_a);

			ConnectedTo conto = new ConnectedTo();
			conto.setContainerName("image_mood_classifier1");
			OperationSignature test_os_b = new OperationSignature();
			test_os_b.setOperationName("predict");
			conto.setOperationSignature(test_os_b);

			ArrayList<ConnectedTo> listofconnto1 = new ArrayList<ConnectedTo>();
			listofconnto1.add(conto);
			test_osl1.setConnectedTo(listofconnto1);

			ArrayList<OperationSignatureList> lofosl1 = new ArrayList<OperationSignatureList>();
			lofosl1.add(test_osl1);
			node1.setOperationSignatureList(lofosl1);

			// Creating node2 and add to the blueprint
			Node node2 = new Node();

			node2.setContainerName("image_mood_classifier1");
			node2.setNodeType("MLModel");
			node2.setImage("someAI-nexus01:8001/image_mood_classifier:1");
			node2.setProtoUri("www.somewhere.com/protourilink2");

			OperationSignatureList test_osl2 = new OperationSignatureList();
			OperationSignature test_os_c = new OperationSignature();

			test_os_c.setOperationName("predict");
			test_os_c.setInputMessageName("someotherinputmsg");
			test_os_c.setOutputMessageName("someotheroutputmsg");

			test_osl2.setOperationSignature(test_os_c);

			ConnectedTo conto2 = new ConnectedTo();
			conto2.setContainerName("null");
			OperationSignature test_os_d = new OperationSignature();
			test_os_d.setOperationName("someotheroperation");
			conto2.setOperationSignature(test_os_d);

			ArrayList<ConnectedTo> listofconnto2 = new ArrayList<ConnectedTo>();
			listofconnto2.add(conto2);
			test_osl2.setConnectedTo(listofconnto2);

			ArrayList<OperationSignatureList> lofosl2 = new ArrayList<OperationSignatureList>();
			lofosl2.add(test_osl2);
			node2.setOperationSignatureList(lofosl2);

			// Creating node3 and add to the blueprint
			Node node3 = new Node();

			node3.setContainerName("Probe1");
			node3.setNodeType("Probe");
			node3.setImage("probeimage");
			node3.setProtoUri("probeprotouri");

			OperationSignatureList test_osl3 = new OperationSignatureList();
			OperationSignature test_os_e = new OperationSignature();

			test_os_e.setOperationName("data");
			test_os_e.setInputMessageName("someotherinputmsg");
			test_os_e.setOutputMessageName("someotheroutputmsg");

			test_osl3.setOperationSignature(test_os_e);

			ArrayList<ConnectedTo> listofconnto3 = new ArrayList<ConnectedTo>();
			listofconnto3.add(null);
			test_osl3.setConnectedTo(listofconnto3);

			ArrayList<OperationSignatureList> lofosl3 = new ArrayList<OperationSignatureList>();
			lofosl3.add(test_osl3);
			node2.setOperationSignatureList(lofosl3);

			bp.addNode(node1);
			bp.addNode(node2);
			bp.addNode(node3);
			// bp.addNode(node4);

			// Create list of Probe indicators and add to the blueprint

			ProbeIndicator testpbindicator = new ProbeIndicator();
			testpbindicator.setValue("true");

			ArrayList<ProbeIndicator> testlistofprobeIndicators = new ArrayList<ProbeIndicator>();
			testlistofprobeIndicators.add(testpbindicator);

			bp.setProbeIndicator(testlistofprobeIndicators);

			// BELOW STUFF CAN BE ADDED ONCE WE START USING TRAINING CLIENT.

			TrainingClient testtc = new TrainingClient();
			testtc.setContainerName("trainingclientname");
			testtc.setImage("some_image");

			DataBroker testdb1 = new DataBroker();
			testdb1.setName("nameofdatabroker");
			OperationSignature dbops = new OperationSignature();
			dbops.setOperationName("getimage");

			List<DataBroker> lodbs = new ArrayList<DataBroker>();
			lodbs.add(testdb1);
			testtc.setDataBrokers(lodbs);

			MlModel testmlmodel1 = new MlModel();
			testmlmodel1.setName("mlmodelname");
			OperationSignature mlops = new OperationSignature();
			dbops.setOperationName("predictimage");
			testmlmodel1.setOperationSignature(mlops);

			List<MlModel> lomlmodels = new ArrayList<MlModel>();
			lomlmodels.add(testmlmodel1);
			testtc.setMlModels(lomlmodels);

			List<TrainingClient> listoftesttc = new ArrayList<TrainingClient>();
			listoftesttc.add(testtc);

			doPut("/putBlueprint", bp, Blueprint.class);
			logger.info("Done testing /putBlueprint PUT end point");

			// testing /putDockerInfo PUT method
			logger.info("Testing /putDockerInfo PUT method");
			DockerInfo docker1 = new DockerInfo();
			docker1.setContainer("image_classifier1");
			docker1.setIpAddress("www.somewhere.com");
			docker1.setPort("8000");

			DockerInfo docker2 = new DockerInfo();
			docker2.setContainer("image_mood_classifier1");
			docker2.setIpAddress("www.somewhere.com");
			docker2.setPort("8001");

			DockerInfo docker3 = new DockerInfo();
			docker3.setContainer("Probe");
			docker3.setIpAddress("www.somewhere.com");
			docker3.setPort("8002");

			DockerInfoList dockerList = new DockerInfoList();
			dockerList.addDockerInfo(docker1);
			dockerList.addDockerInfo(docker2);
			dockerList.addDockerInfo(docker3);

			doPut("/putDockerInfo", dockerList, DockerInfoList.class);

			logger.info("Done testing /putDockerInfo PUT end point");

			// testing the notify method.
			String sampleString = "This is the model connector";
			byte[] b = sampleString.getBytes();
			doPost("/classify", b);
			logger.info("Done testing /{operation} POST end point i.e notify service method");

		} catch (Exception ex) {
			logger.error("Controller Tests failed", ex);
			assert (false);
		}
		assert (true);
	}

	@Test
	public void bpControllerTest2() throws Exception {
		try {

			// TESTING NO BLUEPRING AVAILABLE

			// testing /putDockerInfo PUT method
			logger.info("Testing /putDockerInfo PUT method");
			DockerInfo docker1 = new DockerInfo();
			docker1.setContainer("image_classifier1");
			docker1.setIpAddress("www.somewhere.com");
			docker1.setPort("8000");

			DockerInfo docker2 = new DockerInfo();
			docker2.setContainer("image_mood_classifier1");
			docker2.setIpAddress("www.somewhere.com");
			docker2.setPort("8001");

			DockerInfo docker3 = new DockerInfo();
			docker3.setContainer("Probe");
			docker3.setIpAddress("www.somewhere.com");
			docker3.setPort("8002");

			DockerInfoList dockerList = new DockerInfoList();
			dockerList.addDockerInfo(docker1);
			dockerList.addDockerInfo(docker2);
			dockerList.addDockerInfo(docker3);

			doPut("/putDockerInfo", dockerList, DockerInfoList.class);

			logger.info("Done testing /putDockerInfo PUT end point");
		} catch (Exception ex) {
			logger.error("Controller Tests failed", ex);
			assert (false);
		}
		assert (true);
	}

	@Test
	public void bpControllerTest3() throws Exception {
		try {

			// TESTING NO DOCKERINFO AVAILABLE

			// Test /putBlueprint PUT end point
			logger.info("Testing /putBlueprint PUT method");
			Blueprint bp = new Blueprint();
			bp.setName("Runtime Orchestrator");
			bp.setVersion("1.0.0");

			// Creating Input Port list with 1 input port and add to the
			// blueprint

			InputPort inp1 = new InputPort();

			OperationSignature ios1 = new OperationSignature();
			ios1.setOperationName("classify");

			inp1.setContainerName("image_mood_classifier1");
			inp1.setOperationSignature(ios1);

			List<InputPort> inputportslist = new ArrayList<>();
			inputportslist.add(inp1);

			bp.setInputPorts(inputportslist);

			// Creating node1 and add to the blueprint
			Node node1 = new Node();

			node1.setContainerName("image_classifier1");
			node1.setNodeType("MLModel");
			node1.setImage("someAI-nexus01:8001/image_classifier:1");
			node1.setProtoUri("www.somewhere.com/protourilink");

			OperationSignatureList test_osl1 = new OperationSignatureList();
			OperationSignature test_os_a = new OperationSignature();

			test_os_a.setOperationName("classify");
			test_os_a.setInputMessageName("someinputmsg");
			test_os_a.setOutputMessageName("someoutputmsg");

			test_osl1.setOperationSignature(test_os_a);

			ConnectedTo conto = new ConnectedTo();
			conto.setContainerName("image_mood_classifier1");
			OperationSignature test_os_b = new OperationSignature();
			test_os_b.setOperationName("predict");
			conto.setOperationSignature(test_os_b);

			ArrayList<ConnectedTo> listofconnto1 = new ArrayList<ConnectedTo>();
			listofconnto1.add(conto);
			test_osl1.setConnectedTo(listofconnto1);

			ArrayList<OperationSignatureList> lofosl1 = new ArrayList<OperationSignatureList>();
			lofosl1.add(test_osl1);
			node1.setOperationSignatureList(lofosl1);

			// Creating node2 and add to the blueprint
			Node node2 = new Node();

			node2.setContainerName("image_mood_classifier1");
			node2.setNodeType("MLModel");
			node2.setImage("someAI-nexus01:8001/image_mood_classifier:1");
			node2.setProtoUri("www.somewhere.com/protourilink2");

			OperationSignatureList test_osl2 = new OperationSignatureList();
			OperationSignature test_os_c = new OperationSignature();

			test_os_c.setOperationName("predict");
			test_os_c.setInputMessageName("someotherinputmsg");
			test_os_c.setOutputMessageName("someotheroutputmsg");

			test_osl2.setOperationSignature(test_os_c);

			ConnectedTo conto2 = new ConnectedTo();
			conto2.setContainerName("someothercontainer");
			OperationSignature test_os_d = new OperationSignature();
			test_os_d.setOperationName("someotheroperation");
			conto2.setOperationSignature(test_os_d);

			ArrayList<ConnectedTo> listofconnto2 = new ArrayList<ConnectedTo>();
			listofconnto2.add(conto2);
			test_osl2.setConnectedTo(listofconnto2);

			ArrayList<OperationSignatureList> lofosl2 = new ArrayList<OperationSignatureList>();
			lofosl2.add(test_osl2);
			node2.setOperationSignatureList(lofosl2);

			// Creating node3 and add to the blueprint
			Node node3 = new Node();

			node3.setContainerName("Probe1");
			node3.setNodeType("Probe");
			node3.setImage("probeimage");
			node3.setProtoUri("probeprotouri");

			OperationSignatureList test_osl3 = new OperationSignatureList();
			OperationSignature test_os_e = new OperationSignature();

			test_os_e.setOperationName("data");
			test_os_e.setInputMessageName("someotherinputmsg");
			test_os_e.setOutputMessageName("someotheroutputmsg");

			test_osl3.setOperationSignature(test_os_e);

			ArrayList<ConnectedTo> listofconnto3 = new ArrayList<ConnectedTo>();
			listofconnto3.add(null);
			test_osl3.setConnectedTo(listofconnto3);

			ArrayList<OperationSignatureList> lofosl3 = new ArrayList<OperationSignatureList>();
			lofosl3.add(test_osl3);
			node2.setOperationSignatureList(lofosl3);

			bp.addNode(node1);
			bp.addNode(node2);
			bp.addNode(node3);
			// bp.addNode(node4);

			// Create list of Probe indicators and add to the blueprint

			ProbeIndicator testpbindicator = new ProbeIndicator();
			testpbindicator.setValue("true");

			ArrayList<ProbeIndicator> testlistofprobeIndicators = new ArrayList<ProbeIndicator>();
			testlistofprobeIndicators.add(testpbindicator);

			bp.setProbeIndicator(testlistofprobeIndicators);

			// BELOW STUFF CAN BE ADDED ONCE WE START USING TRAINING CLIENT.

			TrainingClient testtc = new TrainingClient();
			testtc.setContainerName("trainingclientname");
			testtc.setImage("some_image");

			DataBroker testdb1 = new DataBroker();
			testdb1.setName("nameofdatabroker");
			OperationSignature dbops = new OperationSignature();
			dbops.setOperationName("getimage");

			List<DataBroker> lodbs = new ArrayList<DataBroker>();
			lodbs.add(testdb1);
			testtc.setDataBrokers(lodbs);

			MlModel testmlmodel1 = new MlModel();
			testmlmodel1.setName("mlmodelname");
			OperationSignature mlops = new OperationSignature();
			dbops.setOperationName("predictimage");
			testmlmodel1.setOperationSignature(mlops);

			List<MlModel> lomlmodels = new ArrayList<MlModel>();
			lomlmodels.add(testmlmodel1);
			testtc.setMlModels(lomlmodels);

			List<TrainingClient> listoftesttc = new ArrayList<TrainingClient>();
			listoftesttc.add(testtc);

			doPut("/putBlueprint", bp, Blueprint.class);
			logger.info("Done testing /putBlueprint PUT end point");

			logger.info("Done testing /putDockerInfo PUT end point");

			// testing the notify method.
			String sampleString = "This is the model connector";
			byte[] b = sampleString.getBytes();
			doPost("/classify", b);
		} catch (Exception ex) {
			logger.error("Controller Tests failed", ex);
			assert (false);
		}
		assert (true);
	}

	@Test
	public void bpControllerTestHttpGet() throws Exception {
		try {

			org.acumos.bporchestrator.controller.BlueprintOrchestratorController bpcont = new org.acumos.bporchestrator.controller.BlueprintOrchestratorController();
			bpcont.httpGet("www.acumos.org");
		} catch (Exception ex) {
			logger.error("Controller Tests failed", ex);
			assert (true);
		}
		assert (true);
	}

	@Test
	public void internalFunctionsTest() throws Exception {
		try {

			// Test /putBlueprint PUT end point
			logger.info("Testing /putBlueprint PUT method");
			Blueprint bp = new Blueprint();
			bp.setName("Runtime Orchestrator");
			bp.setVersion("1.0.0");

			// Creating Input Port list with 1 input port and add to the
			// blueprint

			InputPort inp1 = new InputPort();

			OperationSignature ios1 = new OperationSignature();
			ios1.setOperationName("classify");

			inp1.setContainerName("image_classifier1");
			inp1.setOperationSignature(ios1);

			List<InputPort> inputportslist = new ArrayList<>();
			inputportslist.add(inp1);

			bp.setInputPorts(inputportslist);

			// Creating node1 and add to the blueprint
			Node node1 = new Node();

			node1.setContainerName("image_classifier1");
			node1.setNodeType("MLModel");
			node1.setImage("someAI-nexus01:8001/image_classifier:1");
			node1.setProtoUri("www.somewhere.com/protourilink");

			OperationSignatureList test_osl1 = new OperationSignatureList();
			OperationSignature test_os_a = new OperationSignature();

			test_os_a.setOperationName("classify");
			test_os_a.setInputMessageName("someinputmsg");
			test_os_a.setOutputMessageName("someoutputmsg");

			test_osl1.setOperationSignature(test_os_a);

			ConnectedTo conto = new ConnectedTo();
			conto.setContainerName("image_mood_classifier1");
			OperationSignature test_os_b = new OperationSignature();
			test_os_b.setOperationName("predict");
			conto.setOperationSignature(test_os_b);

			ArrayList<ConnectedTo> listofconnto1 = new ArrayList<ConnectedTo>();
			listofconnto1.add(conto);
			test_osl1.setConnectedTo(listofconnto1);

			ArrayList<OperationSignatureList> lofosl1 = new ArrayList<OperationSignatureList>();
			lofosl1.add(test_osl1);
			node1.setOperationSignatureList(lofosl1);

			// Creating node2 and add to the blueprint
			Node node2 = new Node();

			node2.setContainerName("image_mood_classifier1");
			node2.setNodeType("MLModel");
			node2.setImage("someAI-nexus01:8001/image_mood_classifier:1");
			node2.setProtoUri("www.somewhere.com/protourilink2");

			OperationSignatureList test_osl2 = new OperationSignatureList();
			OperationSignature test_os_c = new OperationSignature();

			test_os_c.setOperationName("predict");
			test_os_c.setInputMessageName("someotherinputmsg");
			test_os_c.setOutputMessageName("someotheroutputmsg");

			test_osl2.setOperationSignature(test_os_c);

			ConnectedTo conto2 = new ConnectedTo();
			conto2.setContainerName("null");
			OperationSignature test_os_d = new OperationSignature();
			test_os_d.setOperationName("someotheroperation");
			conto2.setOperationSignature(test_os_d);

			ArrayList<ConnectedTo> listofconnto2 = new ArrayList<ConnectedTo>();
			listofconnto2.add(conto2);
			test_osl2.setConnectedTo(listofconnto2);

			ArrayList<OperationSignatureList> lofosl2 = new ArrayList<OperationSignatureList>();
			lofosl2.add(test_osl2);
			node2.setOperationSignatureList(lofosl2);

			// Creating node3 and add to the blueprint
			Node node3 = new Node();

			node3.setContainerName("Probe1");
			node3.setNodeType("Probe");
			node3.setImage("probeimage");
			node3.setProtoUri("probeprotouri");

			OperationSignatureList test_osl3 = new OperationSignatureList();
			OperationSignature test_os_e = new OperationSignature();

			test_os_e.setOperationName("data");
			test_os_e.setInputMessageName("someotherinputmsg");
			test_os_e.setOutputMessageName("someotheroutputmsg");

			test_osl3.setOperationSignature(test_os_e);

			ArrayList<ConnectedTo> listofconnto3 = new ArrayList<ConnectedTo>();
			listofconnto3.add(null);
			test_osl3.setConnectedTo(listofconnto3);

			ArrayList<OperationSignatureList> lofosl3 = new ArrayList<OperationSignatureList>();
			lofosl3.add(test_osl3);
			node2.setOperationSignatureList(lofosl3);

			bp.addNode(node1);
			bp.addNode(node2);
			bp.addNode(node3);
			// bp.addNode(node4);

			// Create list of Probe indicators and add to the blueprint

			ProbeIndicator testpbindicator = new ProbeIndicator();
			testpbindicator.setValue("true");

			ArrayList<ProbeIndicator> testlistofprobeIndicators = new ArrayList<ProbeIndicator>();
			testlistofprobeIndicators.add(testpbindicator);

			bp.setProbeIndicator(testlistofprobeIndicators);

			// BELOW STUFF CAN BE ADDED ONCE WE START USING TRAINING CLIENT.

			TrainingClient testtc = new TrainingClient();
			testtc.setContainerName("trainingclientname");
			testtc.setImage("some_image");

			DataBroker testdb1 = new DataBroker();
			testdb1.setName("nameofdatabroker");
			OperationSignature dbops = new OperationSignature();
			dbops.setOperationName("getimage");

			List<DataBroker> lodbs = new ArrayList<DataBroker>();
			lodbs.add(testdb1);
			testtc.setDataBrokers(lodbs);

			MlModel testmlmodel1 = new MlModel();
			testmlmodel1.setName("mlmodelname");
			OperationSignature mlops = new OperationSignature();
			dbops.setOperationName("predictimage");
			testmlmodel1.setOperationSignature(mlops);

			List<MlModel> lomlmodels = new ArrayList<MlModel>();
			lomlmodels.add(testmlmodel1);
			testtc.setMlModels(lomlmodels);

			List<TrainingClient> listoftesttc = new ArrayList<TrainingClient>();
			listoftesttc.add(testtc);

			doPut("/putBlueprint", bp, Blueprint.class);
			logger.info("Done testing /putBlueprint PUT end point");

			// testing /putDockerInfo PUT method
			logger.info("Testing /putDockerInfo PUT method");
			DockerInfo docker1 = new DockerInfo();
			docker1.setContainer("image_classifier1");
			docker1.setIpAddress("www.somewhere.com");
			docker1.setPort("8000");

			DockerInfo docker2 = new DockerInfo();
			docker2.setContainer("image_mood_classifier1");
			docker2.setIpAddress("www.somewhere.com");
			docker2.setPort("8001");

			DockerInfo docker3 = new DockerInfo();
			docker3.setContainer("Probe");
			docker3.setIpAddress("www.somewhere.com");
			docker3.setPort("8002");

			DockerInfoList dockerList = new DockerInfoList();
			dockerList.addDockerInfo(docker1);
			dockerList.addDockerInfo(docker2);
			dockerList.addDockerInfo(docker3);

			doPut("/putDockerInfo", dockerList, DockerInfoList.class);

			logger.info("Done testing /putDockerInfo PUT end point");

			org.acumos.bporchestrator.controller.BlueprintOrchestratorController bpcont = new org.acumos.bporchestrator.controller.BlueprintOrchestratorController();
			bpcont.traverseEachNode(node1, node2, "sampledata".getBytes(), 0, "Probe", "data");

			org.acumos.bporchestrator.controller.BlueprintOrchestratorController bpcont2 = new org.acumos.bporchestrator.controller.BlueprintOrchestratorController();
			bpcont2.constructProbeUrl("Probe", "data");

			org.acumos.bporchestrator.controller.BlueprintOrchestratorController bpcont3 = new org.acumos.bporchestrator.controller.BlueprintOrchestratorController();

			ConnectedTo connto = new ConnectedTo(node1.getContainerName(),
					node1.getOperationSignatureList().get(0).getOperationSignature());
			List<ConnectedTo> loconnto = new ArrayList();
			loconnto.add(connto);

			bpcont3.splitterSuccessorsOutputAvailable(loconnto);

			org.acumos.bporchestrator.controller.BlueprintOrchestratorController bpcont4 = new org.acumos.bporchestrator.controller.BlueprintOrchestratorController();

			// bpcont4.contactDataBroker("www.acumos.org",
			// node1.getContainerName());

			org.acumos.bporchestrator.controller.BlueprintOrchestratorController bpcont5 = new org.acumos.bporchestrator.controller.BlueprintOrchestratorController();

			bpcont5.allNodesOutputsAvailable();

			// set the all the required attributes.
			DBThreadAttributes dbAttributes = new DBThreadAttributes();
			dbAttributes.setOut("sampledata".getBytes());
			dbAttributes.setpNode(node1);
			dbAttributes.setProbeCont(node1.getContainerName());
			dbAttributes.setProbeOp("classify");
			dbAttributes.setsNode(node2);
			dbAttributes.setId(0);
			dbAttributes.setpNodeHeader(null);
			// create a thread with the dbAttribute objects
			ExecutorService servicetest = Executors.newFixedThreadPool(1);
			servicetest.execute(new DBResponseRunnable(dbAttributes));

			// spawn child node threads
			NewThreadAttributes newThreadAttributes = new NewThreadAttributes();

			// set the all the required attributes.
			newThreadAttributes.setpNode(node1);
			newThreadAttributes.setsNode(node2);
			newThreadAttributes.setOut("sampledata".getBytes());
			newThreadAttributes.setId(0);
			newThreadAttributes.setProbeCont("Probe");
			newThreadAttributes.setProbeOp("data");

			servicetest.execute(new NewModelCaller(newThreadAttributes));

			ProtobufUtil.parseProtobuf(
					" \tsyntax = \"proto3\";\n\t\t\toption java_package = \"com.google.protobuf\";\n\t\t\toption java_outer_classname = \"DatasetProto\";\n\t\t\tservice kaziService {\n\t\t\t  rpc transform (DataFrame) returns (Prediction);\n\t\t\t}\n\t\t\tmessage DataFrameRow {\n\t\t\tint32 C1 = 1;\n\t\t\tint32 C2 = 2;\n\t\t\t}\n\t\t\tmessage DataFrame { \n\t\t\trepeated DataFrameRow rows = 1;\n\t\t\t}\n\t\t\tmessage Prediction {\n\t\t\trepeated double C3 = 1;\n\t\t\t} ");

			/*
			 * ProtobufUtil.parseProtobuf(
			 * "{\"messageName\":\"ComputeInput\",\"messageargumentList\":[{\"role\":\"\",\"name\":\"f1\",\"tag\":\"1\",\"type\":\"double\"},{\"role\":\"\",\"name\":\"f2\",\"tag\":\"2\",\"type\":\"double\"},{\"role\":\"\",\"name\":\"s\",\"tag\":\"3\",\"type\":\"string\"}]}"
			 * );
			 */

			org.acumos.bporchestrator.splittercollator.vo.SplitterMap spmap = new org.acumos.bporchestrator.splittercollator.vo.SplitterMap();
			org.acumos.bporchestrator.splittercollator.vo.CollatorMap comap = new org.acumos.bporchestrator.splittercollator.vo.CollatorMap();

			ObjectMapper mapper = new ObjectMapper();

			// JSON from String to Object
			spmap = mapper.readValue(
					"{\r\n        \"splitter_type\": \"Parameter-based\",\r\n        \"input_message_signature\": \"{\\\"messageName\\\":\\\"ComputeInput\\\",\\\"messageargumentList\\\":[{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f1\\\",\\\"tag\\\":\\\"1\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f2\\\",\\\"tag\\\":\\\"2\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"s\\\",\\\"tag\\\":\\\"3\\\",\\\"type\\\":\\\"string\\\"}]}\",\r\n        \"map_inputs\": [\r\n          {\r\n            \"input_field\": {\r\n              \"parameter_name\": \"f1\",\r\n              \"parameter_type\": \"double\",\r\n              \"parameter_tag\": \"1\",\r\n              \"parameter_role\": \"\"\r\n            }\r\n          },\r\n          {\r\n            \"input_field\": {\r\n              \"parameter_name\": \"f2\",\r\n              \"parameter_type\": \"double\",\r\n              \"parameter_tag\": \"2\",\r\n              \"parameter_role\": \"\"\r\n            }\r\n          },\r\n          {\r\n            \"input_field\": {\r\n              \"parameter_name\": \"s\",\r\n              \"parameter_type\": \"string\",\r\n              \"parameter_tag\": \"3\",\r\n              \"parameter_role\": \"\"\r\n            }\r\n          }\r\n        ],\r\n        \"map_outputs\": [\r\n          {\r\n            \"output_field\": {\r\n              \"target_name\": \"add1\",\r\n              \"message_signature\": \"{\\\"messageName\\\":\\\"ComputeInput\\\",\\\"messageargumentList\\\":[{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f1\\\",\\\"tag\\\":\\\"1\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f2\\\",\\\"tag\\\":\\\"2\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"s\\\",\\\"tag\\\":\\\"3\\\",\\\"type\\\":\\\"string\\\"}]}\",\r\n              \"parameter_tag\": \"1\",\r\n              \"parameter_name\": \"f1\",\r\n              \"parameter_type\": \"double\",\r\n              \"parameter_role\": \"\",\r\n              \"mapped_to_field\": \"1\",\r\n              \"error_indicator\": \"true\"\r\n            }\r\n          },\r\n          {\r\n            \"output_field\": {\r\n              \"target_name\": \"add1\",\r\n              \"message_signature\": \"{\\\"messageName\\\":\\\"ComputeInput\\\",\\\"messageargumentList\\\":[{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f1\\\",\\\"tag\\\":\\\"1\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f2\\\",\\\"tag\\\":\\\"2\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"s\\\",\\\"tag\\\":\\\"3\\\",\\\"type\\\":\\\"string\\\"}]}\",\r\n              \"parameter_tag\": \"2\",\r\n              \"parameter_name\": \"f2\",\r\n              \"parameter_type\": \"double\",\r\n              \"parameter_role\": \"\",\r\n              \"mapped_to_field\": \"2\",\r\n              \"error_indicator\": \"true\"\r\n            }\r\n          },\r\n          {\r\n            \"output_field\": {\r\n              \"target_name\": \"add1\",\r\n              \"message_signature\": \"{\\\"messageName\\\":\\\"ComputeInput\\\",\\\"messageargumentList\\\":[{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f1\\\",\\\"tag\\\":\\\"1\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f2\\\",\\\"tag\\\":\\\"2\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"s\\\",\\\"tag\\\":\\\"3\\\",\\\"type\\\":\\\"string\\\"}]}\",\r\n              \"parameter_tag\": \"3\",\r\n              \"parameter_name\": \"s\",\r\n              \"parameter_type\": \"string\",\r\n              \"parameter_role\": \"\",\r\n              \"mapped_to_field\": \"3\",\r\n              \"error_indicator\": \"true\"\r\n            }\r\n          },\r\n          {\r\n            \"output_field\": {\r\n              \"target_name\": \"multiply1\",\r\n              \"message_signature\": \"{\\\"messageName\\\":\\\"ComputeInput\\\",\\\"messageargumentList\\\":[{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f1\\\",\\\"tag\\\":\\\"1\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f2\\\",\\\"tag\\\":\\\"2\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"s\\\",\\\"tag\\\":\\\"3\\\",\\\"type\\\":\\\"string\\\"}]}\",\r\n              \"parameter_tag\": \"1\",\r\n              \"parameter_name\": \"f1\",\r\n              \"parameter_type\": \"double\",\r\n              \"parameter_role\": \"\",\r\n              \"mapped_to_field\": \"1\",\r\n              \"error_indicator\": \"true\"\r\n            }\r\n          },\r\n          {\r\n            \"output_field\": {\r\n              \"target_name\": \"multiply1\",\r\n              \"message_signature\": \"{\\\"messageName\\\":\\\"ComputeInput\\\",\\\"messageargumentList\\\":[{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f1\\\",\\\"tag\\\":\\\"1\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f2\\\",\\\"tag\\\":\\\"2\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"s\\\",\\\"tag\\\":\\\"3\\\",\\\"type\\\":\\\"string\\\"}]}\",\r\n              \"parameter_tag\": \"2\",\r\n              \"parameter_name\": \"f2\",\r\n              \"parameter_type\": \"double\",\r\n              \"parameter_role\": \"\",\r\n              \"mapped_to_field\": \"2\",\r\n              \"error_indicator\": \"true\"\r\n            }\r\n          },\r\n          {\r\n            \"output_field\": {\r\n              \"target_name\": \"multiply1\",\r\n              \"message_signature\": \"{\\\"messageName\\\":\\\"ComputeInput\\\",\\\"messageargumentList\\\":[{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f1\\\",\\\"tag\\\":\\\"1\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f2\\\",\\\"tag\\\":\\\"2\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"s\\\",\\\"tag\\\":\\\"3\\\",\\\"type\\\":\\\"string\\\"}]}\",\r\n              \"parameter_tag\": \"3\",\r\n              \"parameter_name\": \"s\",\r\n              \"parameter_type\": \"string\",\r\n              \"parameter_role\": \"\",\r\n              \"mapped_to_field\": \"3\",\r\n              \"error_indicator\": \"true\"\r\n            }\r\n          },\r\n          {\r\n            \"output_field\": {\r\n              \"target_name\": \"average1\",\r\n              \"message_signature\": \"{\\\"messageName\\\":\\\"ComputeInput\\\",\\\"messageargumentList\\\":[{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f1\\\",\\\"tag\\\":\\\"1\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f2\\\",\\\"tag\\\":\\\"2\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"s\\\",\\\"tag\\\":\\\"3\\\",\\\"type\\\":\\\"string\\\"}]}\",\r\n              \"parameter_tag\": \"1\",\r\n              \"parameter_name\": \"f1\",\r\n              \"parameter_type\": \"double\",\r\n              \"parameter_role\": \"\",\r\n              \"mapped_to_field\": \"1\",\r\n              \"error_indicator\": \"true\"\r\n            }\r\n          },\r\n          {\r\n            \"output_field\": {\r\n              \"target_name\": \"average1\",\r\n              \"message_signature\": \"{\\\"messageName\\\":\\\"ComputeInput\\\",\\\"messageargumentList\\\":[{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f1\\\",\\\"tag\\\":\\\"1\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f2\\\",\\\"tag\\\":\\\"2\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"s\\\",\\\"tag\\\":\\\"3\\\",\\\"type\\\":\\\"string\\\"}]}\",\r\n              \"parameter_tag\": \"2\",\r\n              \"parameter_name\": \"f2\",\r\n              \"parameter_type\": \"double\",\r\n              \"parameter_role\": \"\",\r\n              \"mapped_to_field\": \"2\",\r\n              \"error_indicator\": \"true\"\r\n            }\r\n          },\r\n          {\r\n            \"output_field\": {\r\n              \"target_name\": \"average1\",\r\n              \"message_signature\": \"{\\\"messageName\\\":\\\"ComputeInput\\\",\\\"messageargumentList\\\":[{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f1\\\",\\\"tag\\\":\\\"1\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f2\\\",\\\"tag\\\":\\\"2\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"s\\\",\\\"tag\\\":\\\"3\\\",\\\"type\\\":\\\"string\\\"}]}\",\r\n              \"parameter_tag\": \"3\",\r\n              \"parameter_name\": \"s\",\r\n              \"parameter_type\": \"string\",\r\n              \"parameter_role\": \"\",\r\n              \"mapped_to_field\": \"3\",\r\n              \"error_indicator\": \"true\"\r\n            }\r\n          }\r\n        ]\r\n      }\r\n    }"

					, org.acumos.bporchestrator.splittercollator.vo.SplitterMap.class);

			comap = mapper.readValue("{\r\n        \"collator_type\": \"Array-based\",\r\n        \"output_message_signature\": \"{\\\"messageName\\\":\\\"ComputeResultList\\\",\\\"messageargumentList\\\":[{\\\"role\\\":\\\"repeated\\\",\\\"complexType\\\":{\\\"messageName\\\":\\\"ComputeResult\\\",\\\"messageargumentList\\\":[{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"f\\\",\\\"tag\\\":\\\"1.1\\\",\\\"type\\\":\\\"double\\\"},{\\\"role\\\":\\\"\\\",\\\"name\\\":\\\"s\\\",\\\"tag\\\":\\\"1.2\\\",\\\"type\\\":\\\"string\\\"}]},\\\"name\\\":\\\"l\\\",\\\"tag\\\":\\\"1\\\",\\\"type\\\":\\\"ComputeResult\\\"}]}\"\r\n      }", org.acumos.bporchestrator.splittercollator.vo.CollatorMap.class);

			ProtobufUtil.parseProtoStr(comap);
			ProtobufUtil.parseProtoStrForSplit(spmap);

		} catch (

		Exception ex) {
			logger.error("Controller Tests failed", ex);
			assert (false);
		}
		assert (true);
	}

}
