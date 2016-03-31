package br.ufmg.dcc.vod.spiderpig.jobs;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;

public interface Request {

	/**
	 * Continue request will keep on crawling this request until it's done
	 * (does not return null).
	 * 
	 * @return {@link CrawlResult} when done. Before it's done, 
	 * it always return's null.
	 * @throws QuotaException While not done.
	 */
	CrawlResult continueRequest() throws QuotaException;
	
}
