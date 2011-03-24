package br.ufmg.dcc.vod.ncrawler.queue;

public class QueueServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public QueueServiceException(String string) {
		super(string);
	}

	public QueueServiceException(InterruptedException e) {
		super(e);
	}

}
