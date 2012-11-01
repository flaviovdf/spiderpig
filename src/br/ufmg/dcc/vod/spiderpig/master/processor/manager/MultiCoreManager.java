package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;

import br.ufmg.dcc.vod.spiderpig.common.ServiceIDUtils;
import br.ufmg.dcc.vod.spiderpig.distributed.worker.WorkerActor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

public class MultiCoreManager implements WorkerManager {
	
	private final ServiceID NULL_ID;
	private final Semaphore semaphore;

	public MultiCoreManager(int numThreads) {
		this.semaphore = new Semaphore(numThreads);
		try {
			this.NULL_ID = 
					 ServiceIDUtils
					 .toResolvedServiceID("localhost", 0, WorkerActor.HANDLE);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
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
	public CrawlID executorSuspected(ServiceID jobExecutor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void markAvailable(ServiceID jobExecutor) {
		throw new UnsupportedOperationException();
	}

}
