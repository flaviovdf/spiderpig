package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import java.util.concurrent.Semaphore;

import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

public class MultiCoreManager implements WorkerManager {
	
	private final Semaphore semaphore;
	private final JobExecutor jobExecutor;

	public MultiCoreManager(int numThreads, JobExecutor jobExecutor) {
		this.jobExecutor = jobExecutor;
		this.semaphore = new Semaphore(numThreads);
	}
	
	@Override
	public Resolver allocateAvailableExecutor(CrawlID crawlID)
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
	public void executorSuspected(ServiceID jobExecutor) {
	}

	@Override
	public void markAvailable(ServiceID jobExecutor) {
	}

}
