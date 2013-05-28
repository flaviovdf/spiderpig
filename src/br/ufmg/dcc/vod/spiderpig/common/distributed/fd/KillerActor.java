package br.ufmg.dcc.vod.spiderpig.common.distributed.fd;

import br.ufmg.dcc.vod.spiderpig.common.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.common.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.common.queue.serializer.MessageLiteSerializer;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.KillResult;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.PingPong;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.ui.EXIT_CODES;

public class KillerActor extends Actor<PingPong> 
		implements QueueProcessor<PingPong> {

	public static final String HANDLE = "Killer";
	private final RemoteMessageSender sender;

	public KillerActor(RemoteMessageSender sender) {
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
		new Thread(new KillerRunnable(t.getCallBackID())).start();
	}

	private class KillerRunnable implements Runnable {

		private final ServiceID callBackID;

		public KillerRunnable(ServiceID callBackID) {
			this.callBackID = callBackID;
		}

		@Override
		public void run() {
			sender.send(callBackID, 
					KillResult.newBuilder().setExitcode(EXIT_CODES.OK).build());
			service.waitUntilWorkIsDoneAndStop(1);
			System.exit(EXIT_CODES.OK);
		}
		
	}
}
