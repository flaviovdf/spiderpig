package br.ufmg.dcc.vod.ncrawler.master;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.jobs.WorkerInterested;
import br.ufmg.dcc.vod.ncrawler.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.WorkerManager;
import br.ufmg.dcc.vod.ncrawler.stats.StatsActor;
import br.ufmg.dcc.vod.ncrawler.tracker.Tracker;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

public class Master implements WorkerInterested {

	private static final Logger LOG = Logger.getLogger(Master.class);
	
	private final Tracker<String> tracker;
	private final ProcessorActor processorActor;
	private final StatsActor statsActor;
	private final WorkerManager workerManager;
	private final ReentrantLock lock;

	public Master(TrackerFactory<String> trackerFactory, 
			ProcessorActor processorActor, StatsActor statsActor,
			WorkerManager workerManager) {
		this.tracker = trackerFactory.createTracker(String.class);
		this.processorActor = processorActor;
		this.statsActor = statsActor;
		this.workerManager = workerManager;
		this.lock = new ReentrantLock();
	}
	
	@Override
	public void crawlDone(String id, List<String> toQueue) {
		LOG.info("Received result for " + id);
		
		this.workerManager.freeExecutor(id);
		try {
			this.lock.lock();
			this.tracker.crawled(id); //Necessary for initial crawl (seed)
			for (String nextID : toQueue) {
				//Returns true if obj is new to tracker, thus we dispatch it
				if (this.tracker.crawled(nextID)) { 
					this.processorActor.dispatch(nextID);
				}
			}
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public void crawlError(String id, String cause, boolean workerSuspected) {
		LOG.info("Received error for " + id + " cause = " + cause);
		
		this.workerManager.freeExecutor(id);
		if (workerSuspected) {
			this.processorActor.dispatch(id);
		} else {
			try {
				this.lock.lock();
				this.tracker.crawled(id); //Necessary for initial crawl (seed)
			} finally {
				this.lock.unlock();
			}
		}
	}
}