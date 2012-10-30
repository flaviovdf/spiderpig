package br.ufmg.dcc.vod.ncrawler.jobs;

import java.util.List;

import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Ids.CrawlID;

/**
 * Common interface for objects interested in crawl results.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface WorkerInterested {

	/**
	 * Indicates that the given id as successfully crawled. The {@code toQueue}
	 * object indicates discovered ids during crawl.
	 * 
	 * @param id ID crawled.
	 * @param toQueue New ids discovered.
	 */
	public void crawlDone(CrawlID id, List<CrawlID> toQueue);
	
	/**
	 * Indicates that the given id failed to be crawled. The cause is given and
	 * the {@code workerSuspected} indicates if the error was caused by the
	 * worker (e.g., it may have failed during the crawl).
	 * 
	 * @param id ID crawled.
	 * @param cause Cause of error.
	 * @param workerSuspected Indicates if this was a worker error.
	 */
	public void crawlError(CrawlID id, String cause, boolean workerSuspected);
	
}
