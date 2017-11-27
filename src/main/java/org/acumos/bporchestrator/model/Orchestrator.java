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
 * Blueprint Orchestrator class
 *
 */
public class Orchestrator implements Serializable {

	private static final long serialVersionUID = 964690784032387784L;
	
	@JsonProperty("name")
	private String name = null;
	
	@JsonProperty("version")
	private String version = null;
	
	@JsonProperty("image")
	private String image = null;
	
	/**
	 * Constructor method
	 * @param name
	 * @param version
	 * @param image
	 */
	public Orchestrator(String name, String version, String image) {
		super();
		this.name = name;
		this.version = version;
		this.image = image;
	}

	/**
	 * POJO no-arg constructor
	 */
	public Orchestrator() {
		super();
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public String toString() {
		return "Orchestrator [name=" + name + ", version=" + version + ", image=" + image + "]";
	}
	
}
