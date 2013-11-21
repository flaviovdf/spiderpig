package br.ufmg.dcc.vod.spiderpig.common.config;

import java.util.Set;

import org.apache.commons.configuration.Configuration;

/**
 * Used in in user interface methods to configure services from a file
 * configuration. Objects which must be configured. 
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface Configurable {

    /**
     * Parses the configuration and returns objects which were 
     * configurated.
     * 
     * @param configuration Configuration to parse
     * @param builder the builder to instantiate any other configurable
     * 
     * @throws BuildException when unable to configurate this object 
     */
    public void configurate(Configuration configuration, 
            ConfigurableBuilder builder) throws BuildException;
    
    /**
     * Get's the set of required parameters for configurating this class
     * 
     * @return Set of parameters names
     */
    public Set<String> getRequiredParameters();
    
}
