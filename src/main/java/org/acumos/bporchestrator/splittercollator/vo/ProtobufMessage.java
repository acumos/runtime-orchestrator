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
import java.util.ArrayList;
import java.util.List;

public class ProtobufMessage implements Serializable {

	private static final long serialVersionUID = -1481673805292740045L;

	private String name;
	private List<ProtobufMessageField> fields;
	
	
	public ProtobufMessage(){
		fields = new ArrayList<ProtobufMessageField>();
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the fields
	 */
	public List<ProtobufMessageField> getFields() {
		return fields;
	}
	/**
	 * @param fields the fields to set
	 */
	public void setFields(List<ProtobufMessageField> fields) {
		this.fields = fields;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("message " + name + " {\n");
		for(ProtobufMessageField f : fields){
			sb.append(f.toString());
		}
		sb.append("}\n");
		return sb.toString();
	}
	
	
	
	
}
