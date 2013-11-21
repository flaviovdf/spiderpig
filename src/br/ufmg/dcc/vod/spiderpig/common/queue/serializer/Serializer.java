package br.ufmg.dcc.vod.spiderpig.common.queue.serializer;

public interface Serializer<T> {

    byte[] toByteArray(T t);
    
    T interpret(byte[] take);

}
