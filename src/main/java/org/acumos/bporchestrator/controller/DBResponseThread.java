package org.acumos.bporchestrator.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.acumos.bporchestrator.MCAttributes;


public class DBResponseThread implements Runnable {

	private MCAttributes mcAttributes;

	public DBResponseThread(MCAttributes mcAttributes) {
		this.mcAttributes = mcAttributes;
	}

	@Override
	public void run() {
		final Logger dbthreadlogger = LoggerFactory.getLogger(DSAsyncResponseThread.class);
		dbthreadlogger.info("New Thread started due to DB response");

		new BlueprintOrchestratorController().notifynextnode(mcAttributes.getOutput(), mcAttributes.getCurrentnode(),
				mcAttributes.getCurrentoperation(), mcAttributes.isProbePresent(), mcAttributes.getProbeContName(),
				mcAttributes.getProbeOperation(), mcAttributes.getProbeurl());

	}

}
