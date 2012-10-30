package br.ufmg.dcc.vod.spiderpig.queue.serializer;

public interface Serializer<T> {

	byte[] toByteArray(T t);
	
	T interpret(byte[] take);

}
