package br.ufmg.dcc.vod.ncrawler.tracker;

public class ThreadSafeTracker<S> implements Tracker<S> {

	private final Tracker<S> t;

	public ThreadSafeTracker(Tracker<S> t) {
		this.t = t;
	}
	
	@Override
	public synchronized boolean add(S s) {
		if (t.contains(s)) {
			return false;
		}
		
		return t.add(s);
	}

	@Override
	public synchronized boolean contains(S s) {
		return t.contains(s);
	}

	@Override
	public synchronized int size() {
		return t.size();
	}
	
}
