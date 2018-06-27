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

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.acumos.bporchestrator.model.Blueprint;
import org.acumos.bporchestrator.model.ConnectedTo;
import org.acumos.bporchestrator.model.DockerInfo;
import org.acumos.bporchestrator.model.DockerInfoList;
import org.acumos.bporchestrator.util.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.acumos.bporchestrator.controller.BlueprintOrchestratorController;

/**
 * Used to parallelly call models connected to the splitter
 */

public class CallMultipleModelParallely implements Callable<Map<String, byte[]>> {
	private static final Logger logger = LoggerFactory.getLogger(BlueprintOrchestratorController.class);
	private byte[] modelOutput = null; // holds output message after calling the
	// node. It is also returned to the
	// client to be added back to the
	// collation list.
	private byte[] depoutput = null; // holds incoming input message for the
	// node.
	private String depcontainername = "";
	private String depoperation = null;
	private String probeurl = null;
	private ArrayList<ConnectedTo> depsofdeps = null;
	private boolean probePresent = false;

	public CallMultipleModelParallely(byte[] depoutput, String depcontainername, String depoperation, String prburl,
			ArrayList<ConnectedTo> depsofdeps, boolean probePresent) {

		this.depoutput = depoutput;
		this.depcontainername = depcontainername;
		this.depoperation = depoperation;
		this.probeurl = prburl;
		this.depoutput = depoutput;
		this.depsofdeps = depsofdeps;
		this.probePresent = probePresent;

	}

	// @Override
	public byte[] call10() throws Exception {
		try {
			DockerInfoList dockerInfoList = TaskManager.getDockerList();
			if (null != dockerInfoList) {
				DockerInfo dockerInfo = dockerInfoList.findDockerInfoByContainer(depcontainername);
				if (null != dockerInfo) {
					modelOutput = new BlueprintOrchestratorController().contactnode(depoutput,
							"http://" + dockerInfo.getIpAddress() + ":" + dockerInfo.getPort() + "/" + depoperation,
							dockerInfo.getContainer());
					// Contact the probe for the model /connectedNode
					if (probePresent == true) {
						new BlueprintOrchestratorController().prepareAndContactprobe(depoperation, probeurl,
								depcontainername, depoutput, modelOutput, depsofdeps, false);
					}

				}

			}
		} catch (Exception ex) {
			logger.error("Exception in CallMultipleModelParallely class and call() method :" + ex);
			throw ex;
		}

		return modelOutput;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, byte[]> call() throws Exception {
		Map<String, byte[]> map = new HashMap<String, byte[]>();
		try {
			DockerInfoList dockerInfoList = TaskManager.getDockerList();
			if (null != dockerInfoList) {
				DockerInfo dockerInfo = dockerInfoList.findDockerInfoByContainer(depcontainername);
				if (null != dockerInfo) {
					modelOutput = new BlueprintOrchestratorController().contactnode(depoutput,
							"http://" + dockerInfo.getIpAddress() + ":" + dockerInfo.getPort() + "/" + depoperation,
							dockerInfo.getContainer());
					// Contact the probe for the model /connectedNode
					if (probePresent == true) {
						new BlueprintOrchestratorController().prepareAndContactprobe(depoperation, probeurl,
								depcontainername, depoutput, modelOutput, depsofdeps, false);
					}

					map.put(depcontainername, modelOutput);
				}

			}
		} catch (SocketTimeoutException ex) {

			/* map.put(depcontainername, new byte[0]); */
			// Thread.sleep(500);
			logger.error("Exception in CallMultipleModelParallely class and call() method :" + ex);
			List<String> listOfModelName = new ArrayList<String>();
			listOfModelName.add(depcontainername);
			if (TaskManager.getListofsocketimoutmodels() != null) {
				TaskManager.getListofsocketimoutmodels().addAll(listOfModelName);
			} else {
				TaskManager.setListofsocketimoutmodels(listOfModelName);
			}

			throw ex;

		} catch (Exception ex) {
			logger.error("Exception in CallMultipleModelParallely class and call() method :" + ex);
			throw ex;
		}

		return map;

	}

}