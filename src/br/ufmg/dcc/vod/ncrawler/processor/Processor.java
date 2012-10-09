package br.ufmg.dcc.vod.ncrawler.processor;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;

/**
 * Dispatches crawl job to be collected.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 *  
 * @param <I> Type of IDs to evaluate
 * @param <C> Type of content being crawled
 */
public interface Processor<I, C> {

	/**
	 * Indicates that the given job should be dispatched.
	 * 
	 * @param crawlJob Job to dispatch
	 */
	public void dispatch(CrawlJob<I, C> crawlJob);
	
}