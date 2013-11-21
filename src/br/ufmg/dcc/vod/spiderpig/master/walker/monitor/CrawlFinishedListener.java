package br.ufmg.dcc.vod.spiderpig.master.walker.monitor;

/**
 * A listener which is notified when the crawl is deemed as finished.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface CrawlFinishedListener {

    /**
     * Notification of crawl done!
     */
    public void crawlDone();

}
