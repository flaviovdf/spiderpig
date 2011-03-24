package br.ufmg.dcc.vod.ncrawler.processor;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;

/**
 * Dispatches crawl job to be collected.
 */
public interface Processor {

	public void dispatch(CrawlJob c);
	
//	public void setEvaluator(Evaluator e);
	
}
