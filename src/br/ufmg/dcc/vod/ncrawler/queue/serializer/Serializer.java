package br.ufmg.dcc.vod.ncrawler.queue.serializer;

public interface Serializer<T> {

	byte[] toByteArray(T t);
	
	T interpret(byte[] take);

}
