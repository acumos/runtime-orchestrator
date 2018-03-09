package org.acumos.bporchestrator.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.acumos.bporchestrator.MCAttributes;

public class DSAsyncResponseRunnable implements Runnable {

	private static final Logger dslogger = LoggerFactory.getLogger(DSAsyncResponseRunnable.class);

	private MCAttributes mcAttributes;

	public DSAsyncResponseRunnable(MCAttributes mcAttributes) {
		this.mcAttributes = mcAttributes;
	}

	@Override
	public void run() {

		dslogger.info("DS Async response thread started");

		new BlueprintOrchestratorController().notifynextnode(mcAttributes.getOutput(), mcAttributes.getCurrentNode(),
				mcAttributes.getCurrentOperation(), mcAttributes.isProbePresent(), mcAttributes.getProbeContName(),
				mcAttributes.getProbeOperation(), mcAttributes.getProbeUrl());

	}
}
