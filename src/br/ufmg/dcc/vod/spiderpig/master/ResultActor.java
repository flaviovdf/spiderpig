package br.ufmg.dcc.vod.spiderpig.master;

import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.BaseResult;
import br.ufmg.dcc.vod.spiderpig.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.queue.serializer.MessageLiteSerializer;

public class ResultActor extends Actor<BaseResult> 
		implements QueueProcessor<BaseResult> {

	public static final String HANDLE = "ResultServer";
	private final WorkerInterested workerInterested;

	public ResultActor(WorkerInterested workerInterested) {
		super(HANDLE);
		this.workerInterested = workerInterested;
	}
	
	@Override
	public QueueProcessor<BaseResult> getQueueProcessor() {
		return this;
	}

	@Override
	public MessageLiteSerializer<BaseResult> newMsgSerializer() {
		return new MessageLiteSerializer<>(BaseResult.newBuilder());
	}

	@Override
	public void process(BaseResult msg) {
		if (msg.getIsError()) {
			workerInterested.crawlError(msg.getId(), msg.getErrorMessage());
		} else {
			workerInterested.crawlDone(msg.getId(), msg.getToQueueList());
		}	
	}

}
