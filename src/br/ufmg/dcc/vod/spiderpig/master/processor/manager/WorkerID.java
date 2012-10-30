package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;

/**
 * Identifies a worker and resolves the {@link JobExecutor} associated with this
 * id.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface WorkerID {

	/**
	 * Resolves this id to an executor.
	 * 
	 * @return a {@link JobExecutor} object
	 */
	public JobExecutor resolve();
	
}
