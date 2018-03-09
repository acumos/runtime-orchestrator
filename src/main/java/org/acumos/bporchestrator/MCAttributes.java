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
