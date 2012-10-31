package br.ufmg.dcc.vod.spiderpig.queue.fd;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.KillResult;
import br.ufmg.dcc.vod.spiderpig.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.queue.serializer.MessageLiteSerializer;

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
