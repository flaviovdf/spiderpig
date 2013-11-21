package br.ufmg.dcc.vod.spiderpig.common.queue.basequeues;

public interface EventQueue<T> {

    public void put(T t);

    public T take();

    public int size();
    
}
