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

public class CollatorMapOutput implements Serializable{
	
	@Override
	public String toString() {
		return "CollatorMapOutput [output_field=" + output_field + "]";
	}

	public CollatorMapOutput() {
		super();
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = -8114356478731475710L;
	
	private CollatorOutputField output_field;

	public CollatorOutputField getOutput_field() {
		return output_field;
	}

	public void setOutput_field(CollatorOutputField output_field) {
		this.output_field = output_field;
	}
	
	
	

}
