package org.acumos.bporchestrator.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.acumos.bporchestrator.MCAttributes;

public class DBResponseRunnable implements Runnable {

	private static final Logger dblogger = LoggerFactory.getLogger(DBResponseRunnable.class);

	private MCAttributes mcAttributes;

	public DBResponseRunnable(MCAttributes mcAttributes) {
		this.mcAttributes = mcAttributes;
	}

	@Override
	public void run() {

		dblogger.info("New Thread started due to DB response");

		new BlueprintOrchestratorController().notifynextnode(mcAttributes.getOutput(), mcAttributes.getCurrentNode(),
				mcAttributes.getCurrentOperation(), mcAttributes.isProbePresent(), mcAttributes.getProbeContName(),
				mcAttributes.getProbeOperation(), mcAttributes.getProbeUrl());

	}

}
