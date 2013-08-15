package br.ufmg.dcc.vod.spiderpig.jobs;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.common.base.Stopwatch;

public class ThroughputManager {

	private static final Logger LOG = Logger.getLogger(ThroughputManager.class);
	private static final int MIN_DELTA = 100;
	
	private final Stopwatch stopwatch;
	
	private final long timeBetweenRequests;
	private final long backoffTime;

	public ThroughputManager(long timeBetweenRequests,
			long backoffTime) {
		this.stopwatch = new Stopwatch();
		this.timeBetweenRequests = timeBetweenRequests;
		this.backoffTime = backoffTime;
		LOG.info("Will sleep every " + timeBetweenRequests + " ms");
	}
	
	private CrawlResult getAndStartWatch(CrawlID crawlID, Requester requester) { 
		
		boolean backoff = true;
		CrawlResult result = null;
		do {
			try {
				result = requester.performRequest(crawlID);
				backoff = false;
			} catch (QuotaException e) {
				LOG.info("Quota Exceeded. Backing off for " + 
						this.backoffTime + "ms -" + e);
				
				if (this.backoffTime > 0) {
					try {
						TimeUnit.MILLISECONDS.sleep(this.backoffTime);
					} catch (InterruptedException ie) {
					}
				}
			}
		} while (backoff);
		
		this.stopwatch.reset();
		this.stopwatch.start();
		
		return result;
	}

	public CrawlResult sleepAndPerform(CrawlID crawlID, Requester requester) {
		if (!this.stopwatch.isRunning()) {
			return getAndStartWatch(crawlID, requester);
		} else {
			long elapsedMillis = this.stopwatch.elapsedMillis();
			long toSleep = timeBetweenRequests - elapsedMillis;
			if (toSleep > MIN_DELTA) {
				LOG.info("" +elapsedMillis + " ms ellapsed since last request."
						 + " sleeping for " + toSleep + " ms");
				try {
					TimeUnit.MILLISECONDS.sleep(toSleep);
				} catch (InterruptedException e) {
				}
			}
			
			return getAndStartWatch(crawlID, requester);
		}
	}
}