package br.ufmg.dcc.vod.ncrawler.tracker;

import br.ufmg.dcc.vod.ncrawler.common.SimpleBloomFilter;

public class BFTracker<S> implements Tracker<S> {

	private final SimpleBloomFilter<S> simpleBloomFilter;

	public BFTracker(SimpleBloomFilter<S> simpleBloomFilter) {
		this.simpleBloomFilter = simpleBloomFilter;
	}

	@Override
	public boolean add(S s) {
		this.simpleBloomFilter.add(s);
		return true;
	}

	@Override
	public boolean contains(S s) {
		return this.simpleBloomFilter.contains(s);
	}

	@Override
	public int size() {
		return this.simpleBloomFilter.size();
	}
}
