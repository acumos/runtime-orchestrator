/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech
 * 						Mahindra. All rights reserved.
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

import org.acumos.bporchestrator.model.Blueprint;
import org.acumos.bporchestrator.model.Component;
import org.acumos.bporchestrator.model.DockerInfo;
import org.acumos.bporchestrator.model.DockerInfoList;
import org.acumos.bporchestrator.model.Node;
import org.acumos.bporchestrator.model.OperationSignature;
import org.acumos.bporchestrator.model.Orchestrator;
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
public class BpControllerTest extends AbstractControllerTest{
	private static Logger logger = LoggerFactory.getLogger(BpControllerTest.class);

	@Test
	public void bpControllerTest() throws Exception {
		try {
			// testing /putDockerInfo PUT method
			logger.info("Testing /putDockerInfo PUT method");
			DockerInfo docker1 = new DockerInfo();
			docker1.setContainer("image_classifier1");
			docker1.setIpAddress("52.191.113.56");
			docker1.setPort("8123");
			
			DockerInfo docker2 = new DockerInfo();
			docker2.setContainer("image_good_classifier1");
			docker2.setIpAddress("52.191.113.56");
			docker2.setPort("8234");
			
			DockerInfoList dockerList = new DockerInfoList();
			dockerList.addDockerInfo(docker1);
			dockerList.addDockerInfo(docker2);
			
			doPut("/putDockerInfo", dockerList, DockerInfoList.class);
			
			logger.info("Done testing /putDockerInfo PUT end point");
			
			// Test /putBlueprint PUT end point
			logger.info("Testing /putBlueprint PUT method");
			Blueprint bp =  new Blueprint();
			Orchestrator orch = new Orchestrator();
			orch.setName("Runtime Orchestrator");
			orch.setVersion("1.0.0");
			orch.setImage("the docker image of the runtime orchestrator");
			bp.setOrchestrator(orch);
			
			Node node1 = new Node();
			
			// Initialize a Component {"name": "image_mood_classifier1", "operation_signature": { "operation": "classify" }}
			Component comp1 = new Component();
			comp1.setName("image_mood_classifier1");
			
			// Use "operation_signature": { "operation": "classify"}
			OperationSignature os =  new OperationSignature();
			os.setOperation("classify");
			comp1.setOperationSignature(os);
			
			node1.setContainerName("image_classifier1");
			node1.setImage("cognita-nexus01:8001/image_classifier:1");
			node1.addDependsOn(comp1);
			
			Node node2 = new Node();
			node2.setContainerName("image_mood_classifier1");
			node2.setImage("cognita-nexus01:8001/image_mood_classifier:1");
			
			Component comp2 = new Component();
			node2.addDependsOn(comp2);
			bp.addNode(node1);
			bp.addNode(node2);
			bp.setName("Real1");
			bp.setVersion("1.1.0");
			
			OperationSignature inputOs = new OperationSignature();
			inputOs.setOperation("classify");
			bp.addInput(inputOs);
			
			doPut("/putBlueprint", bp, Blueprint.class);
			logger.info("Done testing /putBlueprint PUT end point");

		} catch (HttpStatusCodeException ex) {
			logger.error("controllerTest failed", ex);
			assert (false);
		}
		assert (true);
	}

}
