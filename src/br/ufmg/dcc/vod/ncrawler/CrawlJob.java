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

	public void collect();

	public void setEvaluator(Evaluator<I, C> e);

	public I getID();
	
}
