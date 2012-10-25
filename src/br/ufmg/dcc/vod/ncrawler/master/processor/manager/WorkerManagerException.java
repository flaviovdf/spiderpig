package br.ufmg.dcc.vod.ncrawler.master.processor.manager;

import br.ufmg.dcc.vod.ncrawler.common.Constants;

public class WorkerManagerException extends RuntimeException {

	private static final long serialVersionUID = Constants.SERIAL_UID;

	public WorkerManagerException(String msg) {
		super(msg);
	}
}
