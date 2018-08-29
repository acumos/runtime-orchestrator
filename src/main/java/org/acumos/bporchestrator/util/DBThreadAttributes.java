package org.acumos.bporchestrator.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acumos.bporchestrator.model.Node;

public class DBThreadAttributes {

	Node pNode = null;
	Node sNode = null;
	byte[] out = null;
	int id = 0;
	String probeCont = "";
	String probeOp = "";
	Map<String, List<String>> pNodeHeader = new HashMap<String, List<String>>();


	public DBThreadAttributes() {
	}

	public DBThreadAttributes(Node pNode, Node sNode, byte[] out, int id, String probeCont, String probeOp,
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
