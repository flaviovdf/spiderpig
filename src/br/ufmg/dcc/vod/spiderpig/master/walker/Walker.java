package br.ufmg.dcc.vod.spiderpig.master.walker;

import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

/**
 * A walk is responsible for defining the network walk strategy for
 * crawlers. Basically, implementors of this class will return a list
 * of ids to be queued for crawling based on the result of a previous crawled
 * id. 
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface Walker {

	/**
	 * Indicates that the current id was crawled and returns new ids based 
	 * on current result
	 * 
	 * @param crawled ID crawled.
	 * @param links Links discovered
	 * 
	 * @returns id's to crawl
	 */
	public Iterable<CrawlID> walk(CrawlID crawled, Iterable<CrawlID> links);
	
	/**
	 * Indicates to the walker that the following id produced an error.
	 * 
	 * @param idWithError ID crawled.
	 */
	public void errorReceived(CrawlID idWithError);

	/**
	 * Filter seed ID the walker. Seeds are initial ids which may require 
	 * special treatment by different walkers.
	 * 
	 * @param seeds seeds to iterate ID.
	 */
	public Iterable<CrawlID> filterSeeds(Iterable<CrawlID> seeds);

	/**
	 * Gets the {@link StopCondition} which can indicate when the crawl is
	 * done.
	 * 
	 * @return {@link StopCondition} implementation
	 */
	public StopCondition getStopCondition();
	
}