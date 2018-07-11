package org.acumos.bporchestrator.util;

import org.acumos.bporchestrator.model.Node;

public class NewThreadAttributes {

	Node pNode = null;

	Node sNode = null;
	byte[] out = null;
	int id = 0;
	Node pbNode = null;

	public NewThreadAttributes() {
	}

	public NewThreadAttributes(Node pNode, Node sNode, byte[] out, int id, Node pbNode) {
		super();
		this.pNode = pNode;
		this.sNode = sNode;
		this.out = out;
		this.id = id;
		this.pbNode = pbNode;
	}

	public Node getpNode() {
		return pNode;
	}

	public void setpNode(Node pNode) {
		this.pNode = pNode;
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

	public Node getPbNode() {
		return pbNode;
	}

	public void setPbNode(Node pbNode) {
		this.pbNode = pbNode;
	}

}