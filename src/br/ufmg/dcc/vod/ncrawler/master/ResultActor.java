package br.ufmg.dcc.vod.ncrawler.master;

import br.ufmg.dcc.vod.ncrawler.jobs.WorkerInterested;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Worker.BaseResult;
import br.ufmg.dcc.vod.ncrawler.queue.Actor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.serializer.MessageLiteSerializer;

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
			workerInterested.crawlError(msg.getId(), msg.getErrorMessage(), 
					false);
		} else {
			workerInterested.crawlDone(msg.getId(), msg.getToQueueList());
		}	
	}

}
