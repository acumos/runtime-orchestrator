package org.acumos.bporchestrator.controller;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.acumos.bporchestrator.model.ConnectedTo;
import org.acumos.bporchestrator.model.DockerInfo;
import org.acumos.bporchestrator.model.DockerInfoList;
import org.acumos.bporchestrator.util.TaskManager;
import org.acumos.bporchestrator.controller.BlueprintOrchestratorController;

public class CallMultipleModelParallely implements Callable<byte[]> {

	private byte[] modelOutput = null; // holds output message after calling the node. It is also returned to the
										// client to be added back to the collation list.
	private byte[] depoutput = null; // holds incoming input message for the node.
	private String depcontainername = "";
	private String depoperation = null;
	private String probeurl = null;
	ArrayList<ConnectedTo> depsofdeps = null;

	public CallMultipleModelParallely(byte[] depoutput, String depcontainername, String depoperation, String prburl,
			ArrayList<ConnectedTo> depsofdeps) {

		this.depoutput = depoutput;
		this.depcontainername = depcontainername;
		this.depoperation = depoperation;
		this.probeurl = prburl;
		this.depoutput = depoutput;
		this.depsofdeps = depsofdeps;

	}

	@Override
	public byte[] call() throws Exception {

		DockerInfoList dockerInfoList = TaskManager.getDockerList();
		DockerInfo dockerInfo = dockerInfoList.findDockerInfoByContainer(depcontainername);

		modelOutput = new BlueprintOrchestratorController().contactnode(depoutput,
				"http://" + dockerInfo.getIpAddress() + ":" + dockerInfo.getPort() + "/" + depoperation,
				dockerInfo.getContainer());
		// Contact the probe for the model /connectedNode

		new BlueprintOrchestratorController().prepareAndContactprobe(depoperation, probeurl, depcontainername,
				depoutput, modelOutput, depsofdeps);

		return modelOutput;
	}

}
