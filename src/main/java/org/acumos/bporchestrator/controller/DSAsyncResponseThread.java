package org.acumos.bporchestrator.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.acumos.bporchestrator.MCAttributes;

public class DSAsyncResponseThread implements Runnable {
	private MCAttributes mcAttributes;

	public DSAsyncResponseThread(MCAttributes mcAttributes) {
		this.mcAttributes = mcAttributes;
	}

	@Override
	public void run() {

		final Logger dsthreadlogger = LoggerFactory.getLogger(DSAsyncResponseThread.class);
		dsthreadlogger.info("DS Async response thread started");

		new BlueprintOrchestratorController().notifynextnode(mcAttributes.getOutput(), mcAttributes.getCurrentnode(),
				mcAttributes.getCurrentoperation(), mcAttributes.isProbePresent(), mcAttributes.getProbeContName(),
				mcAttributes.getProbeOperation(), mcAttributes.getProbeurl());

	}
}
