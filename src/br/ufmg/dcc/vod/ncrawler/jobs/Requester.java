package br.ufmg.dcc.vod.ncrawler.jobs;

import java.net.URL;

import com.google.common.base.Stopwatch;

public abstract class Requester<T> {

	private static final int MIN_DELTA = 1000;
	
	private final Stopwatch stopwatch;
	private final long timeBetweenRequests;

	public Requester(Stopwatch stopwatch, long timeBetweenRequests) {
		this.stopwatch = stopwatch;
		this.timeBetweenRequests = timeBetweenRequests;
	}
	
	private T getAndStartWatch(URL url) {
		T returnVal = performRequest(url);
		this.stopwatch.start();
		return returnVal;
	}

	public T sleepAndPerform(URL url) {
		if (!this.stopwatch.isRunning()) {
			return getAndStartWatch(url);
		} else {
			long toSleep = timeBetweenRequests - this.stopwatch.elapsedMillis();
			if (toSleep > MIN_DELTA) {
				try {
					Thread.sleep(toSleep);
				} catch (InterruptedException e) {
				}
			}
			this.stopwatch.stop();
			return getAndStartWatch(url);
		}
			
	}

	public abstract T performRequest(URL url);
	
}
