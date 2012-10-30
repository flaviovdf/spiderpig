package br.ufmg.dcc.vod.ncrawler.queue;

import br.ufmg.dcc.vod.ncrawler.common.Constants;

public class QueueServiceException extends RuntimeException {

	private static final long serialVersionUID = Constants.SERIAL_UID;

	public QueueServiceException(String string) {
		super(string);
	}

	public QueueServiceException(Exception e) {
		super(e);
	}

}
