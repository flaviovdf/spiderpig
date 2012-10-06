package br.ufmg.dcc.vod.ncrawler;

import java.io.Serializable;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;

/**
 * Collects an object and returns the result of this crawl.
 * 
 * @param <R> Result from a crawl
 * @param <T> Type of object crawled
 */
public interface CrawlJob extends Serializable {

	public void collect();

	public void setEvaluator(Evaluator e);

	public String getID();
	
}
