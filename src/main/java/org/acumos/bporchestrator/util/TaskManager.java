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

package org.acumos.bporchestrator.util;

import org.acumos.bporchestrator.model.*;

import java.util.List;

import org.acumos.bporchestrator.controller.FinalResults;

/**
 * The TaskManager that holds copies of blueprint.json and DockerInfo.json
 * 
 */
public class TaskManager {

	private static Blueprint blueprint = null;

	private static DockerInfoList dockerList = null;
	private static FinalResults fr = null;
	private static List listOfSockeTimoutModels = null;
	private static int[][] sourceDestinatioNodeMappingTable;

	/**
	 * @return the listofsocketimoutmodels
	 */
	public static List getListofsocketimoutmodels() {
		return listOfSockeTimoutModels;
	}

	/**
	 * @param listofsocketimoutmodels
	 *            the listofsocketimoutmodels to set
	 */
	public static void setListofsocketimoutmodels(List listofsocketimoutmodels) {
		TaskManager.listOfSockeTimoutModels = listofsocketimoutmodels;
	}

	/*
	 * private Constructor for a utility class
	 */
	private TaskManager() {
		throw new IllegalAccessError("TaskManager class");
	}

	public static Blueprint getBlueprint() {
		return blueprint;
	}

	public static void setBlueprint(Blueprint blueprint) {
		TaskManager.blueprint = blueprint;
	}

	public static DockerInfoList getDockerList() {
		return dockerList;
	}

	/**
	 * @return the sourceDestinatioNodeMappingTable
	 */
	public static int[][] getSourceDestinatioNodeMappingTable() {
		return sourceDestinatioNodeMappingTable;
	}

	/**
	 * @param sourceDestinatioNodeMappingTable
	 *            the sourceDestinatioNodeMappingTable to set
	 */
	public static void setSourceDestinatioNodeMappingTable(int[][] sourceDestinatioNodeMappingTable) {
		TaskManager.sourceDestinatioNodeMappingTable = sourceDestinatioNodeMappingTable;
	}

	public static void setDockerList(DockerInfoList dockerList) {
		TaskManager.dockerList = dockerList;
	}

	public static FinalResults getFinalResults() {
		return fr;
	}

	public static void setFinalResults(FinalResults fr) {
		TaskManager.fr = fr;
	}

}
