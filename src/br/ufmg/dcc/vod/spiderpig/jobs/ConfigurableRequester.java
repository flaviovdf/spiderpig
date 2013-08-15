package br.ufmg.dcc.vod.spiderpig.jobs;

import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.common.config.Configurable;

/**
 * Interface which make's {@link Requester}s configurable via the 
 * {@link Configurable} interface.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public abstract class ConfigurableRequester
		extends AbstractConfigurable<Requester> implements Requester {

}
