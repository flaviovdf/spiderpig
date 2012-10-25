package br.ufmg.dcc.vod.ncrawler.distributed.master;

import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.MessageListener;
import br.ufmg.dcc.vod.ncrawler.jobs.WorkerInterested;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Worker.BaseResult;

import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.MessageLite.Builder;

public class ResultListener implements MessageListener<BaseResult> {

	private final WorkerInterested interested;

	public ResultListener(WorkerInterested interested) {
		this.interested = interested;
	}
	
	@Override
	public void receiveMessage(BaseResult msg) {
		if (msg.getIsError()) {
			interested.crawlError(msg.getId(), msg.getErrorMessage(), false);
		} else {
			interested.crawlDone(msg.getId(), msg.getToQueueList());
		}
	}

	@Override
	public ExtensionRegistryLite getRegistry() {
		return null;
	}

	@Override
	public Builder getNewBuilder() {
		return BaseResult.newBuilder();
	}
}