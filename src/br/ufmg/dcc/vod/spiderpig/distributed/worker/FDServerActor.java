package br.ufmg.dcc.vod.spiderpig.distributed.worker;

import br.ufmg.dcc.vod.spiderpig.distributed.nio.service.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.PingPong;
import br.ufmg.dcc.vod.spiderpig.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.queue.serializer.MessageLiteSerializer;

public class FDServerActor extends Actor<PingPong> 
		implements QueueProcessor<PingPong> {

	public static final String HANDLE = "FDServer";
	private final RemoteMessageSender sender;

	public FDServerActor(RemoteMessageSender sender) {
		super(HANDLE);
		this.sender = sender;
	}

	@Override
	public QueueProcessor<PingPong> getQueueProcessor() {
		return this;
	}

	@Override
	public MessageLiteSerializer<PingPong> newMsgSerializer() {
		return new MessageLiteSerializer<>(PingPong.newBuilder());
	}

	@Override
	public void process(PingPong t) {
		sender.send(t.getCallBackID(), 
				PingPong.newBuilder().setCallBackID(getServiceID()).build());
	}

}
