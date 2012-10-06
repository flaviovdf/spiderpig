package br.ufmg.dcc.vod.ncrawler.tracker;

public interface TrackerFactory {

	public <S> Tracker<S> createTracker();
	
}
