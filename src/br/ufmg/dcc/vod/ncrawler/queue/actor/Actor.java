package br.ufmg.dcc.vod.ncrawler.queue.actor;

import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;

public interface Actor<T> extends QueueProcessor<T> {

	public void start();

	public void dispatch(T message);
	
}
