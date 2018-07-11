/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
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

public class Configuration implements Serializable, Cloneable  {

	private static final long serialVersionUID = -1157991065733494655L;

	//private String collator_type;
	private String protobufFileStr;
	private CollatorMapInput[] map_inputs;
	private CollatorMapOutput[] map_outputs;
	

	
	/**
	 * @return the map_inputs
	 */
	public CollatorMapInput[] getMap_inputs() {
		return map_inputs;
	}

	/**
	 * @param map_inputs the map_inputs to set
	 */
	public void setMap_inputs(CollatorMapInput[] map_inputs) {
		this.map_inputs = map_inputs;
	}

	/**
	 * @return the map_outputs
	 */
	public CollatorMapOutput[] getMap_outputs() {
		return map_outputs;
	}

	/**
	 * @param map_outputs the map_outputs to set
	 */
	public void setMap_outputs(CollatorMapOutput[] map_outputs) {
		this.map_outputs = map_outputs;
	}

	/**
	 * @return the collator_type
	 */
	/*public String getCollator_type() {
		return collator_type;
	}*/

	/**
	 * @param collator_type the collator_type to set
	 */
	/*public void setCollator_type(String collator_type) {
		this.collator_type = collator_type;
	}*/

	/**
	 * @return the protobufFileStr
	 */
	public String getProtobufFileStr() {
		return protobufFileStr;
	}

	/**
	 * @param protobufFileStr the protobufFileStr to set
	 */
	public void setProtobufFileStr(String protobufFileStr) {
		this.protobufFileStr = protobufFileStr;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
