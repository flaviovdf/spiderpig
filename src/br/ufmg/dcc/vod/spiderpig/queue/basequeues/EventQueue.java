package br.ufmg.dcc.vod.spiderpig.queue.basequeues;

public interface EventQueue<T> {

	public void put(T t);

	public T take();

	public int size();
	
}
