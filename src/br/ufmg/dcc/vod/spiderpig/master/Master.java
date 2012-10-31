package br.ufmg.dcc.vod.spiderpig.master;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.WorkerManager;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.queue.fd.FDListener;
import br.ufmg.dcc.vod.spiderpig.stats.StatsActor;
import br.ufmg.dcc.vod.spiderpig.tracker.Tracker;
import br.ufmg.dcc.vod.spiderpig.tracker.TrackerFactory;

public class Master implements WorkerInterested, FDListener {

	private static final Logger LOG = Logger.getLogger(Master.class);
	
	private final Tracker<String> tracker;
	private final ProcessorActor processorActor;
	private final StatsActor statsActor;
	private final WorkerManager workerManager;
	private final StopCondition stopCondition;
	private final ReentrantLock lock;

	public Master(TrackerFactory<String> trackerFactory, 
			ProcessorActor processorActor, StatsActor statsActor,
			WorkerManager workerManager) {
		this.tracker = trackerFactory.createTracker(String.class);
		this.processorActor = processorActor;
		this.statsActor = statsActor;
		this.workerManager = workerManager;
		this.stopCondition = new StopCondition();
		this.lock = new ReentrantLock();
	}
	
	public void dispatch(CrawlID crawlID) {
		this.processorActor.dispatch(crawlID);
		this.stopCondition.dispatched();
	}
	
	@Override
	public void crawlDone(CrawlID id, List<CrawlID> toQueue) {
		LOG.info("Received result for " + id);
		
		this.workerManager.freeExecutor(id);
		try {
			this.lock.lock();
			this.tracker.crawled(id.getId()); //Necessary for initial crawl (seed)
			for (CrawlID nextID : toQueue) {
				//Returns true if obj is new to tracker, thus we dispatch it
				if (this.tracker.crawled(nextID.getId())) { 
					dispatch(nextID);
				}
			}
		} finally {
			this.lock.unlock();
		}
		this.stopCondition.resultReceived();
	}

	@Override
	public void crawlError(CrawlID id, String cause) {
		LOG.info("Received error for " + id + " cause = " + cause);
		
		this.workerManager.freeExecutor(id);
		try {
			this.lock.lock();
			//Necessary for initial crawl (seed)
			this.tracker.crawled(id.getId());
		} finally {
			this.lock.unlock();
		}
		this.stopCondition.resultReceived();
	}

	public StopCondition getStopCondition() {
		return stopCondition;
	}

	@Override
	public void isUp(ServiceID serviceID) {
		this.workerManager.markAvailable(serviceID);
	}

	@Override
	public void isSuspected(ServiceID serviceID) {
		this.workerManager.executorSuspected(serviceID);
	}
}