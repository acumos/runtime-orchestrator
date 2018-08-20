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

import org.acumos.bporchestrator.model.Node;

public class MCAttributes {
	public MCAttributes() {
	}

	private Node currentNode;
	private byte[] output = null;
	private String currentOperation;
	private boolean probePresent = false;
	private String probeContName = null;
	private String probeOperation = null;
	private String probeUrl = null;

	/**
	 * @return the current node
	 */
	public Node getCurrentNode() {
		return currentNode;
	}

	/**
	 * @param currentNode
	 *            the current node
	 */
	public void setCurrentNode(Node currentNode) {
		this.currentNode = currentNode;
	}

	/**
	 * @return the output
	 */
	public byte[] getOutput() {
		return output;
	}

	/**
	 * @param output
	 *            the output to set
	 */
	public void setOutput(byte[] output) {
		this.output = output;
	}

	/**
	 * @return the current operation
	 */
	public String getCurrentOperation() {
		return currentOperation;
	}

	/**
	 * @param currentOperation
	 *            the current operation
	 */
	public void setCurrentOperation(String currentOperation) {
		this.currentOperation = currentOperation;
	}

	/**
	 * @return the probePresent
	 */
	public boolean isProbePresent() {
		return probePresent;
	}

	/**
	 * @param probePresent
	 *            the probePresent to set
	 */
	public void setProbePresent(boolean probePresent) {
		this.probePresent = probePresent;
	}

	/**
	 * @return the probeContName
	 */
	public String getProbeContName() {
		return probeContName;
	}

	/**
	 * @param probeContName
	 *            the probeContName to set
	 */
	public void setProbeContName(String probeContName) {
		this.probeContName = probeContName;
	}

	/**
	 * @return the probeOperation
	 */
	public String getProbeOperation() {
		return probeOperation;
	}

	/**
	 * @param probeOperation
	 *            the probeOperation to set
	 */
	public void setProbeOperation(String probeOperation) {
		this.probeOperation = probeOperation;
	}

	/**
	 * @return the probeurl
	 */
	public String getProbeUrl() {
		return probeUrl;
	}

	/**
	 * @param probeUrl
	 *            the probeurl to set
	 */
	public void setProbeUrl(String probeUrl) {
		this.probeUrl = probeUrl;
	}

}
