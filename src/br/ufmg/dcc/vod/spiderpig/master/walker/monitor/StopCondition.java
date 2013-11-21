package br.ufmg.dcc.vod.spiderpig.master.walker.monitor;

import br.ufmg.dcc.vod.spiderpig.master.Master;

/**
 * A {@link StopCondition} will monitor certain crawl events until it deems
 * the crawl as finished. The most simple of these conditions are the:
 * {@link ExhaustCondition} which stops when the queue is empty and the
 * {@link NeverEndingCondition} which makes the crawl run forever. The second
 * is useful if you have ids being generated and added to the crawler 
 * independent of the crawl queue. When it thinks the crawl should finished,
 * the condition notifies the {@links CrawlFinishedListener} objects which
 * were registered.
 * 
 * The {@link Master} object which controls the crawl will notify certain
 * actions to the conditions so that it can unblock the waiter thread.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface StopCondition {

    /**
     * Indicates that an id has been dispatched for crawling.
     */
    public void dispatched();

    /**
     * Indicates that an 
     */
    public void resultReceived();

    /**
     * Indicates that an error has been received.
     */
    public void errorReceived();
    
    /**
     * Adds a listener to be notified when the crawl is considered finished.
     * 
     * @param listener Listener to notify.
     */
    public void addCrawlFinishedListener(CrawlFinishedListener listener);
    
}
