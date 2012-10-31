package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

/**
 * Identifies a worker and resolves the {@link JobExecutor} associated with this
 * id.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface Resolver {

	/**
	 * Resolves this id to an executor.
	 * 
	 * @param {@link ServiceID} a service ID to resolve
	 *  
	 * @return a {@link JobExecutor} object
	 */
	public JobExecutor resolve(ServiceID serviceID);
	
}
