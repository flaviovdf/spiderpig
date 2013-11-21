package br.ufmg.dcc.vod.spiderpig.common.distributed.fd;

import br.ufmg.dcc.vod.spiderpig.common.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.common.queue.serializer.MessageLiteSerializer;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.KillResult;

public class CallBackKillActor extends Actor<KillResult> 
        implements QueueProcessor<KillResult> {

    public static final String HANDLE = "CallBackKill";

    public CallBackKillActor() {
        super(HANDLE);
    }

    @Override
    public QueueProcessor<KillResult> getQueueProcessor() {
        return this;
    }

    @Override
    public MessageLiteSerializer<KillResult> newMsgSerializer() {
        return new MessageLiteSerializer<>(KillResult.newBuilder());
    }

    @Override
    public void process(KillResult t) {
        System.exit(t.getExitcode());
    }
}
