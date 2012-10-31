package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import java.util.concurrent.Semaphore;

import br.ufmg.dcc.vod.spiderpig.common.ServiceIDUtils;
import br.ufmg.dcc.vod.spiderpig.distributed.worker.WorkerActor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

public class MultiCoreManager implements WorkerManager {
	
	private static final ServiceID NULL_ID = 
			ServiceIDUtils.toServiceID("localhost", 0, WorkerActor.HANDLE);
	private final Semaphore semaphore;

	public MultiCoreManager(int numThreads) {
		this.semaphore = new Semaphore(numThreads);
	}
	
	@Override
	public ServiceID allocateAvailableExecutor(CrawlID crawlID)
			throws InterruptedException {
		this.semaphore.acquire();
		return NULL_ID;
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
