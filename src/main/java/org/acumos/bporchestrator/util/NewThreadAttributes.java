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

package org.acumos.bporchestrator.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acumos.bporchestrator.model.Node;

public class NewThreadAttributes {

	Node pNode = null;
	Node sNode = null;
	byte[] out = null;
	int id = 0;
	String probeCont = "";
	String probeOp = "";
	Map<String, List<String>> pNodeHeader = new HashMap<String, List<String>>();

	public NewThreadAttributes() {
	}

	public NewThreadAttributes(Node pNode, Node sNode, byte[] out, int id, String probeCont, String probeOp,
			Map<String, List<String>> pNodeHeader) {
		super();
		this.pNode = pNode;
		this.sNode = sNode;
		this.out = out;
		this.id = id;
		this.probeCont = probeCont;
		this.probeOp = probeOp;
		this.pNodeHeader = pNodeHeader;
	}

	public Node getpNode() {
		return pNode;
	}

	public void setpNode(Node pNode) {
		this.pNode = pNode;
	}

	public String getProbeCont() {
		return probeCont;
	}

	public void setProbeCont(String probeCont) {
		this.probeCont = probeCont;
	}

	public String getProbeOp() {
		return probeOp;
	}

	public void setProbeOp(String probeOp) {
		this.probeOp = probeOp;
	}

	public Node getsNode() {
		return sNode;
	}

	public void setsNode(Node sNode) {
		this.sNode = sNode;
	}

	public byte[] getOut() {
		return out;
	}

	public void setOut(byte[] out) {
		this.out = out;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Map<String, List<String>> getpNodeHeader() {
		return pNodeHeader;
	}

	public void setpNodeHeader(Map<String, List<String>> pNodeHeader) {
		this.pNodeHeader = pNodeHeader;
	}

}