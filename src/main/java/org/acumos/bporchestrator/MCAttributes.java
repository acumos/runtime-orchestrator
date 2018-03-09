package org.acumos.bporchestrator;

import org.acumos.bporchestrator.model.Node;

public class MCAttributes {
	public MCAttributes() {
	}

	private Node currentnode;
	private byte[] output = null;
	private String currentoperation;
	private boolean probePresent = false;
	private String probeContName = null;
	private String probeOperation = null;
	private String probeurl = null;

	/**
	 * @return the current node
	 */
	public Node getCurrentnode() {
		return currentnode;
	}

	/**
	 * @param currentnode
	 *            the current node
	 */
	public void setCurrentnode(Node currentnode) {
		this.currentnode = currentnode;
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
	public String getCurrentoperation() {
		return currentoperation;
	}

	/**
	 * @param currentoperation
	 *            the current operation
	 */
	public void setCurrentoperation(String currentoperation) {
		this.currentoperation = currentoperation;
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
	public String getProbeurl() {
		return probeurl;
	}

	/**
	 * @param probeurl
	 *            the probeurl to set
	 */
	public void setProbeurl(String probeurl) {
		this.probeurl = probeurl;
	}

}
