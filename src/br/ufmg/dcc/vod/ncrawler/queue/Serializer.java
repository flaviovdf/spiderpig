package br.ufmg.dcc.vod.ncrawler.queue;

public interface Serializer<T> {

	public byte[] checkpointData(T t);
	
	public T interpret(byte[] checkpoint);
	
}
