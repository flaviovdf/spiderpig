package br.ufmg.dcc.vod.ncrawler.distributed.worker;

import java.util.List;

import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.NIOMessageSender;
import br.ufmg.dcc.vod.ncrawler.jobs.WorkerInterested;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Worker.BaseResult;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Worker.BaseResult.Builder;

public class InterestedProxy implements WorkerInterested {

	private final String callBackHost;
	private final int callBackPort;
	private final NIOMessageSender sender;

	public InterestedProxy(String callBackHost, int callBackPort,
			NIOMessageSender sender) {
		this.callBackHost = callBackHost;
		this.callBackPort = callBackPort;
		this.sender = sender;
	}

	@Override
	public void crawlDone(String id, List<String> toQueue) {
		Builder builder = BaseResult.newBuilder();
		builder.setIsError(false);
		builder.setId(id);
		builder.addAllToQueue(toQueue);
		
		sender.send(this.callBackHost, this.callBackPort, builder.build());
	}
	
	@Override
	public void crawlError(String id, String cause, boolean workerSuspected) {
		Builder builder = BaseResult.newBuilder();
		builder.setIsError(true);
		builder.setErrorMessage(cause);
		builder.setId(id);
		
		sender.send(this.callBackHost, this.callBackPort, builder.build());
	}
}