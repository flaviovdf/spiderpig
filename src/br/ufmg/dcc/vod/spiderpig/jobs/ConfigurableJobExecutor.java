package br.ufmg.dcc.vod.spiderpig.jobs;

import br.ufmg.dcc.vod.spiderpig.common.config.Configurable;

/**
 * Interface which make's {@link JobExecutor}s configurable via the 
 * {@link Configurable} interface.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface ConfigurableJobExecutor
		extends Configurable<Void>, JobExecutor {

}
