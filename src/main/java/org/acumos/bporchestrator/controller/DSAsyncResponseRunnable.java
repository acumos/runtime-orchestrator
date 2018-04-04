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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.acumos.bporchestrator.MCAttributes;

public class DSAsyncResponseRunnable implements Runnable {

	private static final Logger dslogger = LoggerFactory.getLogger(DSAsyncResponseRunnable.class);

	private MCAttributes mcAttributes;

	public DSAsyncResponseRunnable(MCAttributes mcAttributes) {
		this.mcAttributes = mcAttributes;
	}

	@Override
	public void run() {

		dslogger.info("DS Async response thread started");

		try {
			new BlueprintOrchestratorController().notifynextnode(mcAttributes.getOutput(), mcAttributes.getCurrentNode(),
					mcAttributes.getCurrentOperation(), mcAttributes.isProbePresent(), mcAttributes.getProbeContName(),
					mcAttributes.getProbeOperation(), mcAttributes.getProbeUrl());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
