package br.ufmg.dcc.vod.spiderpig.common.config;

import org.apache.commons.configuration.Configuration;

/**
 * Used in in user interface methods to configure services from a file
 * configuration. Objects which must be configured. 
 * 
 * TODO: It may be better to have implementors of this class to be factories
 *       of other objects. Currently, classes which implement this will be tied
 *       to a single configuration. I'll keep it like this for now.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface Configurable<T extends Arguments> {

	/**
	 * Parses the configuration and returns objects as {@link Arguments}
	 * 
	 * @param configuration Configuration to parse
	 * 
	 * @return Arguments to return
	 * 
	 * @throws Exception if unable to parse configuration.
	 */
	public T configurate(Configuration configuration) throws Exception;
	
}
