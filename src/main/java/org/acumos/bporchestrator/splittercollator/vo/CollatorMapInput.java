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

package org.acumos.bporchestrator.splittercollator.vo;

import java.io.Serializable;

public class CollatorMapInput implements Serializable {

	@Override
	public String toString() {
		return "CollatorMapInput [input_field=" + input_field + "]";
	}

	public CollatorMapInput() {
		super();
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 7290422172814728241L;
	
	private CollatorInputField input_field;

	public CollatorInputField getInput_field() {
		return input_field;
	}
	
	public void setInput_field(CollatorInputField input_field) {
		this.input_field = input_field;
	}

}
