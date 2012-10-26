package br.ufmg.dcc.vod.ncrawler.jobs;

import com.google.common.base.Stopwatch;

public class ThroughputManager {

	private static final int MIN_DELTA = 100;
	
	private final Stopwatch stopwatch;
	private final long timeBetweenRequests;

	public ThroughputManager(long timeBetweenRequests) {
		this.stopwatch = new Stopwatch();
		this.timeBetweenRequests = timeBetweenRequests;
	}
	
	private <T> T getAndStartWatch(String crawlID, Requester<T> requester) 
			throws Exception {
		T returnVal = requester.performRequest(crawlID);
		this.stopwatch.start();
		return returnVal;
	}

	public <T> T sleepAndPerform(String crawlID, Requester<T> requester) 
			throws Exception {
		if (!this.stopwatch.isRunning()) {
			return getAndStartWatch(crawlID, requester);
		} else {
			long toSleep = timeBetweenRequests - this.stopwatch.elapsedMillis();
			if (toSleep > MIN_DELTA) {
				try {
					Thread.sleep(toSleep);
				} catch (InterruptedException e) {
				}
			}
			this.stopwatch.reset();
			return getAndStartWatch(crawlID, requester);
		}
	}	
}