package br.ufmg.dcc.vod.spiderpig.master.walker;

import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public class ThreadSafeWalker implements Walker {

    private final Walker walker;

    public ThreadSafeWalker(Walker walker) {
        this.walker = walker;
    }
    
    @Override
    public synchronized StopCondition getStopCondition() {
        return this.walker.getStopCondition();
    }

    @Override
    public synchronized Iterable<CrawlID> walk(CrawlID crawled, 
            Iterable<CrawlID> links) {
        return this.walker.walk(crawled, links);
    }

    @Override
    public synchronized Iterable<CrawlID> filterSeeds(Iterable<CrawlID> seeds) {
        return this.walker.filterSeeds(seeds);
    }

    @Override
    public synchronized void errorReceived(CrawlID idWithError) {
        this.walker.errorReceived(idWithError);
    }
    
}