package br.ufmg.dcc.vod.spiderpig.common.config;

import br.ufmg.dcc.vod.spiderpig.common.Constants;

/**
 * Signals error when building a configurable
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class BuildException extends Exception {

	private static final long serialVersionUID = Constants.SERIAL_UID;
	
	public BuildException(String msg, Throwable cause) {
		super(msg, cause);
	}
}