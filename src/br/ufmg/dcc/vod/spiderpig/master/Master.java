package br.ufmg.dcc.vod.spiderpig.master;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.FDListener;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
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
	private final WorkerManager workerManager;
	private final Cache<CrawlID, List<CrawlID>> cache;

	public Master(Walker walker, WorkerManager workerManager, 
			Cache<CrawlID, List<CrawlID>> cache) {
		this.walker = walker;
		this.workerManager = workerManager;
		this.cache = cache;
	}
	
	public void addSeed(List<String> seeds) {
		CrawlID.Builder builder = CrawlID.newBuilder();
		for (String seed : seeds) {
			CrawlID crawlID = builder.setId(seed).build();
			LOG.info("Adding Seed " + crawlID);
			this.walker.addSeedID(crawlID);
		}
		
		this.walker.dispatchSeeds();
	}
	
	public void addSeed(File seedFile) throws IOException {
		BufferedReader br = null;
		String line;
		CrawlID.Builder builder = CrawlID.newBuilder();
		try	{
			br = new BufferedReader(new FileReader(seedFile));
			while((line = br.readLine()) != null) {
				CrawlID crawlID = builder.setId(line).build();
				LOG.info("Adding Seed " + crawlID);
				this.walker.addSeedID(crawlID);
			}
			
			br.close();
		} finally {
			if(br != null) {
				br.close();
			}
		}
		
		this.walker.dispatchSeeds();
	}
	
	@Override
	public void crawlDone(CrawlID id, List<CrawlID> toQueue) {
		LOG.info("Received result for " + id);
		
		if (this.cache != null)
			this.cache.put(id, toQueue);
		this.workerManager.freeExecutor(id);
		this.walker.dispatchNext(id, toQueue);
	}

	@Override
	public void crawlError(CrawlID id, String cause) {
		LOG.info("Received error for " + id + " cause = " + cause);
		
		this.walker.errorReceived(id);
		this.workerManager.freeExecutor(id);
	}

	public StopCondition getStopCondition() {
		return this.walker.getStopCondition();
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
			this.walker.workerFailedWithID(cid);
	}
}