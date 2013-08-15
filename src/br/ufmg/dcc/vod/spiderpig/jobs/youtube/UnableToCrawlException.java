package br.ufmg.dcc.vod.spiderpig.jobs.youtube;

import br.ufmg.dcc.vod.spiderpig.common.Constants;

public class UnableToCrawlException extends Exception {

	private static final long serialVersionUID = Constants.SERIAL_UID;
	
	public UnableToCrawlException(Exception cause) {
		super(cause);
	}
}
