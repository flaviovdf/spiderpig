package br.ufmg.dcc.vod.ncrawler.tracker;

public interface Tracker<S> {
	
	public boolean add(S s);
	
	public boolean contains(S s);
	
	public int size();
	
}
