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
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dependent of each Model Node in the blueprint.json
 */
public class Component implements Serializable {

	private static final long serialVersionUID = 5749775315078650369L;

	@JsonProperty("name")
	private String name = null;

	@JsonProperty("operation_signature")
	private OperationSignature operationSignature = null;

	/**
	 * Standard POJO no-arg constructor
	 */
	public Component() {
		super();
	}

	/**
	 * Component Constructor
	 * 
	 * @param name
	 *            Name
	 * @param operationSignature
	 *            Operation signature
	 */
	public Component(String name, OperationSignature operationSignature) {
		super();
		this.name = name;
		this.operationSignature = operationSignature;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OperationSignature getOperationSignature() {
		return operationSignature;
	}

	public void setOperationSignature(OperationSignature operationSignature) {
		this.operationSignature = operationSignature;
	}

	@Override
	public String toString() {
		return "Component [name=" + name + ", operationSignature=" + operationSignature + "]";
	}

}
