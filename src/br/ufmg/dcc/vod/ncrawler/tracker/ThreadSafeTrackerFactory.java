package br.ufmg.dcc.vod.ncrawler.tracker;

import br.ufmg.dcc.vod.ncrawler.common.SimpleBloomFilter;

public class ThreadSafeTrackerFactory implements TrackerFactory {

	private static final int TEN_MILLION = 10000000;
	
	@Override
	public <S> Tracker<S> createTracker() {
		return new ThreadSafeTracker<S>(new BFTracker<S>(new SimpleBloomFilter<S>(5 * TEN_MILLION * 16, 5 * TEN_MILLION)));
	}

}
