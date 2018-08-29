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

import org.acumos.bporchestrator.util.DBThreadAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBResponseRunnable implements Runnable {

	private static final Logger dblogger = LoggerFactory.getLogger(DBResponseRunnable.class);

	private DBThreadAttributes dbAttributes;

	public DBResponseRunnable(DBThreadAttributes dbAttributes) {
		this.dbAttributes = dbAttributes;
	}

	@Override
	public void run() {

		Thread.currentThread().setName(dbAttributes.getsNode().getContainerName() + "thread");

		dblogger.info("DB thread  {} i.e {} started by {} for {} with input as {}", Thread.currentThread().getId(),
				Thread.currentThread().getName(), dbAttributes.getpNode().getContainerName().toUpperCase(),
				dbAttributes.getsNode().getContainerName().toUpperCase(), dbAttributes.getOut());

		try {

			new BlueprintOrchestratorController().traverseEachNode(dbAttributes.getpNode(), dbAttributes.getsNode(),
					dbAttributes.getOut(), dbAttributes.getId(), dbAttributes.getProbeCont(),
					dbAttributes.getProbeOp());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}