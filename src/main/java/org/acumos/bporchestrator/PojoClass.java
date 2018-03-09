package org.acumos.bporchestrator;

import org.acumos.bporchestrator.model.Node;

public class PojoClass {
	public PojoClass() {
	}

	private Node inpnode;
	private byte[] output = null;
	private String inpoperation;
	private boolean probePresent = false;
	private String probeContName = null;
	private String probeOperation = null;
	private String probeurl = null;

	/**
	 * @return the inpnode
	 */
	public Node getInpnode() {
		return inpnode;
	}

	/**
	 * @param inpnode
	 *            the inpnode to set
	 */
	public void setInpnode(Node inpnode) {
		this.inpnode = inpnode;
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
	 * @return the inpoperation
	 */
	public String getInpoperation() {
		return inpoperation;
	}

	/**
	 * @param inpoperation
	 *            the inpoperation to set
	 */
	public void setInpoperation(String inpoperation) {
		this.inpoperation = inpoperation;
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
