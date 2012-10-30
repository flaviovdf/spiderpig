package br.ufmg.dcc.vod.spiderpig.queue.basequeues;

import java.util.LinkedList;

public class SimpleEventQueue<T> implements EventQueue<T> {

	private LinkedList<T> list = new LinkedList<T>();
	
	@Override
	public void put(T t) {
		list.addLast(t);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public T take() {
		return list.removeFirst();
	}

}
