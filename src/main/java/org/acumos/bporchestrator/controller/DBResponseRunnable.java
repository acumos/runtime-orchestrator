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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.acumos.bporchestrator.MCAttributes;

public class DBResponseRunnable implements Runnable {

	private static final Logger dblogger = LoggerFactory.getLogger(DBResponseRunnable.class);

	private MCAttributes mcAttributes;

	public DBResponseRunnable(MCAttributes mcAttributes) {
		this.mcAttributes = mcAttributes;
	}

	@Override
	public void run() {

		dblogger.info("New Thread started due to DB response");

		try {
			new BlueprintOrchestratorController().notifyNextNode(mcAttributes.getOutput(), mcAttributes.getCurrentNode(),
					mcAttributes.getCurrentOperation(), mcAttributes.isProbePresent(), mcAttributes.getProbeContName(),
					mcAttributes.getProbeOperation(), mcAttributes.getProbeUrl());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
