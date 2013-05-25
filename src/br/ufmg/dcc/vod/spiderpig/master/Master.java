package br.ufmg.dcc.vod.spiderpig.master;

import java.util.List;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.distributed.fd.FDListener;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.WorkerManager;
import br.ufmg.dcc.vod.spiderpig.master.walker.Walker;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.worker.WorkerActor;

import com.google.common.cache.Cache;

public class Master implements WorkerInterested, FDListener {

	private static final Logger LOG = Logger.getLogger(Master.class);
	
	private final Walker walker;
	private final ProcessorActor processorActor;
	private final WorkerManager workerManager;
	private final StopCondition stopCondition;
	private final Cache<CrawlID, List<CrawlID>> cache;

	public Master(Walker walker, ProcessorActor processorActor, 
			WorkerManager workerManager, Cache<CrawlID, List<CrawlID>> cache) {
		this.walker = walker;
		this.processorActor = processorActor;
		this.workerManager = workerManager;
		this.stopCondition = this.walker.getStopCondition();
		this.cache = cache;
	}
	
	public void addSeed(List<CrawlID> crawlIDs) {
		for (CrawlID crawlID : crawlIDs) {
			LOG.info("Adding Seed " + crawlID);
			walker.addSeedID(crawlID);
		}
		
		List<CrawlID> toDispatch = walker.getSeedDispatch();
		if (toDispatch != null) {
			for (CrawlID crawlID : toDispatch) {
				LOG.info("Dispatching Seed " + crawlID);
				dispatch(crawlID, true);
			}
		}
	}
	
	private void dispatch(CrawlID crawlID, boolean incrementCount) {
		this.processorActor.dispatch(crawlID);
		if (incrementCount)
			this.stopCondition.dispatched();
	}
	
	@Override
	public void crawlDone(CrawlID id, List<CrawlID> toQueue) {
		LOG.info("Received result for " + id);
		
		this.workerManager.freeExecutor(id);
		for (CrawlID nextID : this.walker.getToWalk(id, toQueue)) {
			dispatch(nextID, true);
		}
		
		if (this.cache != null)
			this.cache.put(id, toQueue);
		this.stopCondition.resultReceived();
	}

	@Override
	public void crawlError(CrawlID id, String cause) {
		LOG.info("Received error for " + id + " cause = " + cause);
		
		this.workerManager.freeExecutor(id);
		this.stopCondition.errorReceived();
	}

	public StopCondition getStopCondition() {
		return stopCondition;
	}

	@Override
	public void isUp(ServiceID serviceID) {
		ServiceID workerID = ServiceID.newBuilder(serviceID)
				.setHandle(WorkerActor.HANDLE).build();
		LOG.info("Worker up " + workerID);
		this.workerManager.markAvailable(workerID);
	}

	@Override
	public void isSuspected(ServiceID serviceID) {
		ServiceID workerID = ServiceID.newBuilder(serviceID)
				.setHandle(WorkerActor.HANDLE).build();
		CrawlID cid = this.workerManager.executorSuspected(workerID);
		LOG.info("Worker suspected " + workerID);
		if (cid != null)
			dispatch(cid, false); //Does not count as new dispatch
	}
}