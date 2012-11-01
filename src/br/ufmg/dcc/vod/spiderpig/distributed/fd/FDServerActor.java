package br.ufmg.dcc.vod.spiderpig.distributed.fd;

import br.ufmg.dcc.vod.spiderpig.distributed.nio.service.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.PingPong;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.queue.serializer.MessageLiteSerializer;

public class FDServerActor extends Actor<PingPong> 
		implements QueueProcessor<PingPong> {

	public static final String HANDLE = "FDServer";
	private final RemoteMessageSender sender;
	protected PingPong msg;

	public FDServerActor(RemoteMessageSender sender) {
		super(HANDLE);
		this.sender = sender;
	}

	@Override
	public void process(PingPong t) {
		ServiceID callBackID = t.getCallBackID();
		PingPong build = getMsg();
		sender.send(callBackID, build);
	}

	private PingPong getMsg() {
		if (this.msg != null)
			return this.msg;
		
		this.msg = PingPong.newBuilder()
				.setCallBackID(getServiceID())
				.setSessionID(service.getSessionID())
				.build();
		return this.msg;
	}

	@Override
	public QueueProcessor<PingPong> getQueueProcessor() {
		return this;
	}

	@Override
	public MessageLiteSerializer<PingPong> newMsgSerializer() {
		return new MessageLiteSerializer<>(PingPong.newBuilder());
	}
}