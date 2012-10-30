package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

/**
 * Common interface for worker managers. Implementers of this interface must
 * deal with managing {@link WorkerID} resources. Each {@link WorkerID} 
 * is allocated to one {@link JobExecutor}.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface WorkerManager {

	/**
	 * Get's an available {@link WorkerID} blocking if necessary.
	 * 
	 * @param crawlID The id of the crawl the worker will execute
	 * 
	 * @return a {@link WorkerID}
	 * @throws InterruptedException Thrown if blocking fails 
	 */
	public WorkerID allocateAvailableExecutor(CrawlID crawlID) 
			throws InterruptedException;
	
	/**
	 * Free's a {@link WorkerID} marking it as available to execute new
	 * crawls. 
	 * 
	 * @param crawlID The id of the crawl the worker was executing
	 */
	public boolean freeExecutor(CrawlID crawlID);
	
	/**
	 * Mark's a {@link WorkerID} as suspected. Suspected executors will
	 * not be used to execute crawls unless they are re-inserted with the
	 * {@code WorkerManager#markAvailable} method.
	 * 
	 * @param jobExecutor
	 */
	public void executorSuspected(WorkerID jobExecutor);
	
	/**
	 * Mark's a {@link WorkerID} as available for executing tasks.
	 * 
	 * @param jobExecutor {@link WorkerID} to mark as available
	 */ 
	public void markAvailable(WorkerID jobExecutor);
	
}
