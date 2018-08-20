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

public class MlModel implements Serializable {
	private final static long serialVersionUID = 4186812809255376373L;

	@JsonProperty("name")
	private String name = null;
	@JsonProperty("operation_signature")
	private OperationSignature operationSignature = null;

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public MlModel() {
		super();
	}

	/**
	 * 
	 * @param operationSignature
	 *            This is the operation signature
	 * @param name
	 *            This is the model name
	 * 
	 */
	public MlModel(String name, OperationSignature operationSignature) {
		super();
		this.name = name;
		this.operationSignature = operationSignature;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("operation_signature")
	public OperationSignature getOperationSignature() {
		return operationSignature;
	}

	@JsonProperty("operation_signature")
	public void setOperationSignature(OperationSignature operationSignature) {
		this.operationSignature = operationSignature;
	}

}