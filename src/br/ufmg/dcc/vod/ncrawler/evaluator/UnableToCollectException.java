package br.ufmg.dcc.vod.ncrawler.evaluator;

import br.ufmg.dcc.vod.ncrawler.common.Constants;

public class UnableToCollectException extends Exception {

	private static final long serialVersionUID = Constants.SERIAL_UID;

	public UnableToCollectException(String message) {
		super(message);
	}

}
