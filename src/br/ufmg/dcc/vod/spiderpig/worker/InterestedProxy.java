package br.ufmg.dcc.vod.spiderpig.worker;

import br.ufmg.dcc.vod.spiderpig.common.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;

public class InterestedProxy implements WorkerInterested {

	private final RemoteMessageSender sender;
	private final ServiceID callBackID;

	public InterestedProxy(ServiceID callBackID, RemoteMessageSender sender) {
		this.callBackID = callBackID;
		this.sender = sender;
	}

	@Override
	public void crawlDone(CrawlResult crawlResult) {
		sender.send(callBackID, crawlResult);
	}
}