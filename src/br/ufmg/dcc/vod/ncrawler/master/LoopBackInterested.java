package br.ufmg.dcc.vod.ncrawler.master;

import java.util.List;

import br.ufmg.dcc.vod.ncrawler.jobs.WorkerInterested;

public class LoopBackInterested implements WorkerInterested {

	private WorkerInterested wi;

	@Override
	public void crawlDone(String id, List<String> toQueue) {
		wi.crawlDone(id, toQueue);
	}

	@Override
	public void crawlError(String id, String cause, boolean workerSuspected) {
		wi.crawlError(id, cause, workerSuspected);
	}
	
	public void setLoopBack(WorkerInterested wi) {
		this.wi = wi;
	}
}