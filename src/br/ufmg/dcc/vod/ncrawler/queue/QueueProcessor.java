package br.ufmg.dcc.vod.ncrawler.queue;

public interface QueueProcessor<T> {

	public void process(T t);

	public String getName();
	
}
