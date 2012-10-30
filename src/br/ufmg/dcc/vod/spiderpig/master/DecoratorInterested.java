package br.ufmg.dcc.vod.spiderpig.master;

import java.util.List;

import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public class DecoratorInterested implements WorkerInterested {

	private WorkerInterested wi;

	@Override
	public void crawlDone(CrawlID id, List<CrawlID> toQueue) {
		wi.crawlDone(id, toQueue);
	}

	@Override
	public void crawlError(CrawlID id, String cause, boolean workerSuspected) {
		wi.crawlError(id, cause, workerSuspected);
	}
	
	public void setLoopBack(WorkerInterested wi) {
		this.wi = wi;
	}
}