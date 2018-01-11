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
 * 
 * Operation Signature that contains API end point of the model
 *
 */
public class OperationSignature implements Serializable {

	private static final long serialVersionUID = -8176878378145971860L;

	@JsonProperty("operation")
	private String operation = null;

	/**
	 * Standard POJO no-arg constructor
	 */
	public OperationSignature() {
		super();
	}

	/**
	 * Constructor initializing fields
	 * @param operation
	 * Operation
	 */
	public OperationSignature(String operation) {
		super();
		this.operation = operation;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	@Override
	public String toString() {
		return "OperationSignature [operation=" + operation + "]";
	}

}
