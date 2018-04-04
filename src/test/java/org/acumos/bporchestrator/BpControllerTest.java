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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.acumos.bporchestrator.controller.BlueprintOrchestratorController;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.web.client.HttpStatusCodeException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class BpControllerTest extends AbstractControllerTest {
	private static Logger logger = LoggerFactory.getLogger(BpControllerTest.class);

	@Test
	public void bpControllerTest() throws Exception {
		try {

			// Test /putBlueprint PUT end point
			logger.info("Testing /putBlueprint PUT method");
			Blueprint bp = new Blueprint();
			bp.setName("Runtime Orchestrator");
			bp.setVersion("1.0.0");

			// Creating Input Port list with 1 input port and add to the blueprint

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
			node1.setImage("cognita-nexus01:8001/image_classifier:1");
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
			node2.setImage("cognita-nexus01:8001/image_mood_classifier:1");
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
			testpbindicator.setValue("false");

			ArrayList<ProbeIndicator> testlistofprobeIndicators = new ArrayList<ProbeIndicator>();
			testlistofprobeIndicators.add(testpbindicator);

			bp.setProbeIndicator(testlistofprobeIndicators);

			// BELOW STUFF CAN BE ADDED ONCE WE START USING TRAINING CLIENT.
			/*
			 * 
			 * TrainingClient testtc = new TrainingClient();
			 * testtc.setContainerName("trainingclientname"); testtc.setImage("some_image");
			 * 
			 * 
			 * 
			 * DataBroker testdb1 = new DataBroker(); testdb1.setName("nameofdatabroker");
			 * OperationSignature dbops = new OperationSignature();
			 * dbops.setOperationName("getimage");
			 * 
			 * testtc.setDataBrokers(testdb1);
			 * 
			 * 
			 * 
			 * MlModel testmlmodel1 = new MlModel(); testmlmodel1.setName("mlmodelname");
			 * OperationSignature mlops = new OperationSignature();
			 * dbops.setOperationName("predictimage");
			 * testmlmodel1.setOperationSignature(mlops);
			 * 
			 * testtc.setMlModel(testmlmodel1);
			 * 
			 * List<TrainingClient> listoftesttc = new List<TrainingClient>();
			 * listoftesttc.add(testtc);
			 * 
			 */

			doPut("/putBlueprint", bp, Blueprint.class);
			logger.info("Done testing /putBlueprint PUT end point");

			// testing /putDockerInfo PUT method
			logger.info("Testing /putDockerInfo PUT method");
			DockerInfo docker1 = new DockerInfo();
			docker1.setContainer("image_classifier1");
			docker1.setIpAddress("52.191.113.56");
			docker1.setPort("8123");

			DockerInfo docker2 = new DockerInfo();
			docker2.setContainer("image_mood_classifier1");
			docker2.setIpAddress("52.191.113.56");
			docker2.setPort("8234");

			DockerInfoList dockerList = new DockerInfoList();
			dockerList.addDockerInfo(docker1);
			dockerList.addDockerInfo(docker2);

			doPut("/putDockerInfo", dockerList, DockerInfoList.class);

			logger.info("Done testing /putDockerInfo PUT end point");
			
			//testing the notify method.
			String sampleString = "This is the model connector";
			byte[] b = sampleString.getBytes();
			doPost("/classify",b);
			logger.info("Done testing /{operation} POST end point i.e notify service method");
		

		} catch (HttpStatusCodeException ex) {
			logger.error("controllerTest failed", ex);
			assert (false);
		}
		assert (true);
	}

}
