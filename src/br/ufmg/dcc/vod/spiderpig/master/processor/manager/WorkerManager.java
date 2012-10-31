package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

/**
 * Common interface for worker managers. Implementers of this interface must
 * deal with managing {@link ServiceID} resources. Each {@link ServiceID} 
 * is allocated to one {@link JobExecutor}.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface WorkerManager {

	/**
	 * Get's an available {@link ServiceID} blocking if necessary.
	 * 
	 * @param crawlID The id of the crawl the worker will execute
	 * 
	 * @return a {@link Resolver} which proxies message to remote server
	 * @throws InterruptedException Thrown if blocking fails 
	 */
	public Resolver allocateAvailableExecutor(CrawlID crawlID) 
			throws InterruptedException;
	
	/**
	 * Free's a {@link ServiceID} marking it as available to execute new
	 * crawls. 
	 * 
	 * @param crawlID The id of the crawl the worker was executing
	 */
	public boolean freeExecutor(CrawlID crawlID);
	
	/**
	 * Mark's a {@link ServiceID} as suspected. Suspected executors will
	 * not be used to execute crawls unless they are re-inserted with the
	 * {@code WorkerManager#markAvailable} method.
	 * 
	 * @param jobExecutor
	 */
	public void executorSuspected(ServiceID jobExecutor);
	
	/**
	 * Mark's a {@link WorkerID} as available for executing tasks.
	 * 
	 * @param jobExecutor {@link ServiceID} to mark as available
	 */ 
	public void markAvailable(ServiceID jobExecutor);
	
}
