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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrainingClient implements Serializable {

	@JsonProperty("container_name")
	private String containerName;
	@JsonProperty("image")
	private String image;
	@JsonProperty("data_brokers")
	private List<DataBroker> dataBrokers = null;
	@JsonProperty("ml_models")
	private List<MlModel> mlModels = null;
	private final static long serialVersionUID = 6803294465759068422L;

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public TrainingClient() {
	}

	/**
	 * 
	 * @param dataBrokers
	 *            This is the databroker
	 * @param containerName
	 *            This is is the container name
	 * @param image
	 *            This is the image
	 * @param mlModels
	 *            This is the mlmodels to train.
	 */
	public TrainingClient(String containerName, String image, List<DataBroker> dataBrokers, List<MlModel> mlModels) {
		super();
		this.containerName = containerName;
		this.image = image;
		this.dataBrokers = dataBrokers;
		this.mlModels = mlModels;
	}

	@JsonProperty("container_name")
	public String getContainerName() {
		return containerName;
	}

	@JsonProperty("container_name")
	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	@JsonProperty("image")
	public String getImage() {
		return image;
	}

	@JsonProperty("image")
	public void setImage(String image) {
		this.image = image;
	}

	@JsonProperty("data_brokers")
	public List<DataBroker> getDataBrokers() {
		return dataBrokers;
	}

	@JsonProperty("data_brokers")
	public void setDataBrokers(List<DataBroker> dataBrokers) {
		this.dataBrokers = dataBrokers;
	}

	@JsonProperty("ml_models")
	public List<MlModel> getMlModels() {
		return mlModels;
	}

	@JsonProperty("ml_models")
	public void setMlModels(List<MlModel> mlModels) {
		this.mlModels = mlModels;
	}

}