package org.acumos.bporchestrator.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.acumos.bporchestrator.PojoClass;

public class ModelConnectorDStThread implements Runnable {
	private PojoClass pojoClass;

	public ModelConnectorDStThread(PojoClass pojoClass) {
		this.pojoClass = pojoClass;
	}

	@Override
	public void run() {
		System.out.println("ModelConnectorDStThread Thread started");
		try {
			Thread.sleep(500);
			new BlueprintOrchestratorController().notifynextnode(pojoClass.getOutput(), pojoClass.getInpnode(), pojoClass.getInpoperation(),
					pojoClass.isProbePresent(), pojoClass.getProbeContName(), pojoClass.getProbeOperation(),
					pojoClass.getProbeurl());
		} catch (InterruptedException ex) {
		} finally {
		}

	}
}
