package br.ufmg.dcc.vod.ncrawler;

import java.io.Serializable;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;

/**
 * Collects an object and returns the result of this crawl.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 * 
 * @param <I> ID for crawling
 * @param <C> Type of object crawled
 */
public interface CrawlJob<I, C> extends Serializable {

	/**
	 * Performs the collection. After collection, the {@link Evaluator} should
	 * be notified of results.
	 * 
	 * @return
	 */
	public void collect();

	/**
	 * Set's the evaluator to be notified by results.
	 * 
	 * @param evaluator {@link Evaluator} evaluator to receive results.
	 */
	public void setEvaluator(Evaluator<I, C> evaluator);

	/**
	 * @return The ID of the content which will be collected by this job.
	 */
	public I getID();
	
}
