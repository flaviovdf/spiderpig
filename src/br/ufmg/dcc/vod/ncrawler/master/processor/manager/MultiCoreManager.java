package br.ufmg.dcc.vod.ncrawler.master.processor.manager;

import java.util.concurrent.Semaphore;

import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Ids.CrawlID;

public class MultiCoreManager implements WorkerManager {
	
	private final Semaphore semaphore;
	private final JobExecutor jobExecutor;

	public MultiCoreManager(int numThreads, JobExecutor jobExecutor) {
		this.jobExecutor = jobExecutor;
		this.semaphore = new Semaphore(numThreads);
	}
	
	@Override
	public WorkerID allocateAvailableExecutor(CrawlID crawlID)
			throws InterruptedException {
		this.semaphore.acquire();
		return new DumbID(this.jobExecutor);
	}

	@Override
	public boolean freeExecutor(CrawlID crawlID) {
		this.semaphore.release();
		return true;
	}

	@Override
	public void executorSuspected(WorkerID jobExecutor) {
	}

	@Override
	public void markAvailable(WorkerID jobExecutor) {
	}

}
