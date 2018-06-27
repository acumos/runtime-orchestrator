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
import java.util.List;

import org.acumos.bporchestrator.model.InputPort;
import org.acumos.bporchestrator.model.Node;
import org.acumos.bporchestrator.model.ProbeIndicator;
import org.acumos.bporchestrator.model.TrainingClient;

public class FinalResults {

	private byte[] finalResults;
	private String msgName;

	public FinalResults() {
		super();
	}

	// Constructor
	public FinalResults(byte[] res, String msg) {
		super();
		this.finalResults = res;
		this.msgName = msg;
	}

	public String getMsgname() {
		return msgName;
	}

	public void setMsgname(String msg) {
		this.msgName = msg;
	}

	public byte[] getFinalresults() {
		return finalResults;
	}

	public void setFinalresults(byte[] results) {
		this.finalResults = results;
	}

}
