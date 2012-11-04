package br.ufmg.dcc.vod.spiderpig.common.config;

/**
 * Used to represent null return values for arguments.
 *  
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public final class VoidArguments implements Arguments {

	private VoidArguments() { };
	
	/**
	 * @return Always returns null and this is expected. Void arguments are
	 * a simple hack to represent configurables with no return
	 */
	public static VoidArguments defaultInstance() { return null; };
	
}
