package br.ufmg.dcc.vod.spiderpig.jobs;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;

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
     * @param crawlResult Result that was crawled.
     */
    public void crawlDone(CrawlResult crawlResult);
    
}
