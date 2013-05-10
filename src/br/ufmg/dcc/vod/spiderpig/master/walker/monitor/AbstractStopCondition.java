package br.ufmg.dcc.vod.spiderpig.master.walker.monitor;

import java.util.ArrayList;

/**
 * Contains base methods to be used by {@link StopCondition} implementations.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public abstract class AbstractStopCondition implements StopCondition {

	private ArrayList<CrawlFinishedListener> listeners;

	public AbstractStopCondition() {
		this.listeners = new ArrayList<CrawlFinishedListener>();
	}
	
	@Override
	public final void addCrawlFinishedListener(CrawlFinishedListener listener) {
		this.listeners.add(listener);
	}
	
	protected final void notifyAllListeners() {
		for (CrawlFinishedListener listener : this.listeners)
			listener.crawlDone();
	}

}
