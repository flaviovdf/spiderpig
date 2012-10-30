package br.ufmg.dcc.vod.spiderpig.stats;

import br.ufmg.dcc.vod.spiderpig.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.queue.serializer.MessageLiteSerializer;

import com.google.protobuf.MessageLite;

public class StatsActor extends Actor<MessageLite> {

	private static final String HANDLE = "StatsActor";
	private Display display;
	
	public StatsActor(QueueService service) {
		super(HANDLE);
	}

	/**
	 * SHOULD BE CALLED BEFORE THREAD STARTS!
	 */
	public void setDisplay(Display display) {
		this.display = display;
	}

	@Override
	public QueueProcessor<MessageLite> getQueueProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageLiteSerializer<MessageLite> newMsgSerializer() {
		// TODO Auto-generated method stub
		return null;
	}

}