package br.ufmg.dcc.vod.spiderpig.master.walker;

import br.ufmg.dcc.vod.spiderpig.common.config.Configurable;

/**
 * Interface which make's {@link Walker}s configurable via the 
 * {@link Configurable} interface.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface ConfigurableWalker 
        extends Walker, Configurable {

}
