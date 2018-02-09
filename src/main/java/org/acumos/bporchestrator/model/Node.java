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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model Node in the composite solution as specified in the blueprint.json
 */
public class Node implements Serializable {

	private static final long serialVersionUID = 1900236928331958666L;

	@JsonProperty("container_name")
	private String containerName = null;

	@JsonProperty("image")
	private String image = null;

	@JsonProperty("depends_on")
	private ArrayList<Component> dependsOn = null;

	@JsonProperty("proto_url")
	private String protoUrl = null;

	@JsonProperty("node_type")
	private String nodeType = null;

	@JsonProperty("message_name")
	private String messageName = null;

	/**
	 * Standard POJO no-arg constructor
	 */
	public Node() {
		super();
	}

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public ArrayList<Component> getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(ArrayList<Component> dependsOn) {
		this.dependsOn = dependsOn;
	}

	public Node addDependsOn(Component component) {
		if (this.dependsOn == null) {
			this.dependsOn = new ArrayList<>();
		}
		this.dependsOn.add(component);
		return this;
	}

	public String getProtoUrl() {
		return protoUrl;
	}

	public void setProtoUrl(String protoUrl) {
		this.protoUrl = protoUrl;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public String getMessageName() {
		return messageName;
	}

	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}
	
	@Override
	public String toString() {
		return "Node [containerName=" + containerName + ", image=" + image + ", dependsOn=" + dependsOn + ", protoUrl="
				+ protoUrl + ", nodeType=" + nodeType + ", messageName=" + messageName + "]";
	}
}
