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

package org.acumos.bporchestrator.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of a Node in the composite solution as specified in the
 * blueprint.json
 */

public class Node implements Serializable {

	private static final long serialVersionUID = 3561091761587012180L;

	@JsonProperty("container_name")
	private String container = null;

	@JsonProperty("node_type")
	private String nodeType = null;

	@JsonProperty("image")
	private String image = null;

	@JsonProperty("proto_uri")
	private String protoUri = null;

	@JsonProperty("operation_signature_list")
	private ArrayList<OperationSignatureList> operationSignatureList = null; // OperationSignatureList
																				// itself
																				// is
																				// NOT
																				// a
																				// Arraylist
	@JsonProperty("data_sources")
	private List<DataSource> dataSources = null;

	@JsonProperty("data_broker_map")
	private DataBrokerMap dataBrokerMap;

	@JsonProperty("collator_map")
	private CollatorMap collatorMap;

	@JsonProperty("splitter_map")
	private SplitterMap splitterMap;

	private boolean outputAvailable = false;

	private byte[] nodeOutput = null;

	private List<Node> immediateAncestors = new ArrayList<Node>();

	public boolean beingProcessedByAThread = false;

	private Map<String, List<String>> nodeHeaders = new HashMap<String, List<String>>();

	/**
	 * Standard POJO no-arg constructor
	 */
	public Node() {
		super();
	}

	/**
	 * @param operationSignatureList
	 *            List of operations supported by the node
	 * @param protoUri
	 *            Url of protofile : required to be passed to the Probe
	 * @param container
	 *            Name of the container
	 * @param image
	 *            Url of the docker image of the named node in Nexus.
	 *            Information consumed by deployer
	 * @param dataSources
	 *            Required by the data broker
	 * @param nodeType
	 *            Type of the node: DataMapper or MLModel or DataBroker or
	 *            TrainingClient or Probe
	 * @param dataBrokerMap
	 *            Data broker info data structure.
	 * @param collatorMap
	 *            Collator info structure
	 * @param splitterMap
	 *            Splitter info structure
	 * @param outputAvailable
	 *            Says if the node's output is available.
	 * @param nodeOutput
	 *            The node's output after it is called.
	 * @param immediateAncestors
	 *            The immediate ancestors of the node.
	 * @param beingProcessedByAThread
	 *            Says if the node is being processed by a thread
	 * @param nodeHeaders
	 *            Http Headers received from the model's response.
	 */
	public Node(String container, String nodeType, String image, String protoUri,
			ArrayList<OperationSignatureList> operationSignatureList, List<DataSource> dataSources,
			DataBrokerMap dataBrokerMap, CollatorMap collatorMap, SplitterMap splitterMap, boolean outputAvailable,
			byte[] nodeOutput, List<Node> immediateAncestors, boolean beingProcessedByAThread,
			Map<String, List<String>> nodeHeaders) {
		super();
		this.container = container;
		this.nodeType = nodeType;
		this.image = image;
		this.protoUri = protoUri;
		this.operationSignatureList = operationSignatureList;
		this.dataSources = dataSources;
		this.dataBrokerMap = dataBrokerMap;
		this.collatorMap = collatorMap;
		this.splitterMap = splitterMap;
		this.outputAvailable = outputAvailable;
		this.nodeOutput = nodeOutput;
		this.immediateAncestors = immediateAncestors;
		this.beingProcessedByAThread = beingProcessedByAThread;
		this.nodeHeaders = nodeHeaders;
	}

	@JsonProperty("container_name")
	public String getContainerName() {
		return container;
	}

	@JsonProperty("container_name")
	public void setContainerName(String container) {
		this.container = container;
	}

	@JsonProperty("node_type")
	public String getNodeType() {
		return nodeType;
	}

	@JsonProperty("node_type")
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	@JsonProperty("image")
	public String getImage() {
		return image;
	}

	@JsonProperty("image")
	public void setImage(String image) {
		this.image = image;
	}

	@JsonProperty("proto_uri")
	public String getProtoUri() {
		return protoUri;
	}

	@JsonProperty("proto_uri")
	public void setProtoUri(String protoUri) {
		this.protoUri = protoUri;
	}

	@JsonProperty("operation_signature_list")
	public ArrayList<OperationSignatureList> getOperationSignatureList() {
		return operationSignatureList;
	}

	@JsonProperty("operation_signature_list")
	public void setOperationSignatureList(ArrayList<OperationSignatureList> operationSignatureList) {
		this.operationSignatureList = operationSignatureList;
	}

	@JsonProperty("data_sources")
	public List<DataSource> getDataSources() {
		return dataSources;
	}

	@JsonProperty("data_sources")
	public void setDataSources(List<DataSource> dataSources) {
		this.dataSources = dataSources;
	}

	@JsonProperty("data_broker_map")
	public DataBrokerMap getDataBrokerMap() {
		return dataBrokerMap;
	}

	@JsonProperty("data_broker_map")
	public void setDataBrokerMap(DataBrokerMap dataBrokerMap) {
		this.dataBrokerMap = dataBrokerMap;
	}

	@JsonProperty("collator_map")
	public CollatorMap getCollatorMap() {
		return collatorMap;
	}

	@JsonProperty("collator_map")
	public void setCollatorMap(CollatorMap collatorMap) {
		this.collatorMap = collatorMap;
	}

	public boolean isOutputAvailable() {
		return outputAvailable;
	}

	public void setOutputAvailable(boolean outputAvailable) {
		this.outputAvailable = outputAvailable;
	}

	public byte[] getNodeOutput() {
		return nodeOutput;
	}

	public void setNodeOutput(byte[] nodeOutput) {
		this.nodeOutput = nodeOutput;
	}

	public List<Node> getImmediateAncestors() {
		return immediateAncestors;
	}

	public void setImmediateAncestors(List<Node> immediateAncestors) {
		this.immediateAncestors = immediateAncestors;
	}

	public SplitterMap getSplitterMap() {
		return splitterMap;
	}

	public void setSplitterMap(SplitterMap splitterMap) {
		this.splitterMap = splitterMap;
	}

	public boolean isBeingProcessedByAThread() {
		return beingProcessedByAThread;
	}

	public void setBeingProcessedByAThread(boolean beingProcessedByAThread) {
		this.beingProcessedByAThread = beingProcessedByAThread;
	}

	public Map<String, List<String>> getNodeHeaders() {
		return nodeHeaders;
	}

	public void setNodeHeaders(Map<String, List<String>> nodeHeaders) {
		this.nodeHeaders = nodeHeaders;
	}

	public boolean immediateAncestorsOutputAvailable() {
		// Check all ancestors for output
		for (Node n : this.immediateAncestors)

		{
			if (n.isOutputAvailable() == false) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "Node [container=" + container + ", image=" + image + ", protoUri=" + protoUri + ", nodeType=" + nodeType
				+ ", dataBrokerMap=" + dataBrokerMap + ",collatorMap=" + collatorMap + ",splitterMap=" + splitterMap
				+ ",outputAvailable=" + outputAvailable + ",nodeOutput=" + nodeOutput + ",immediateAncestors="
				+ immediateAncestors + "]";
	}

}