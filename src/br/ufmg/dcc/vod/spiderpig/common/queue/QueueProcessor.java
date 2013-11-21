package br.ufmg.dcc.vod.spiderpig.common.queue;

import com.google.protobuf.MessageLite;

public interface QueueProcessor<T extends MessageLite> {

    public void process(T t);

}
