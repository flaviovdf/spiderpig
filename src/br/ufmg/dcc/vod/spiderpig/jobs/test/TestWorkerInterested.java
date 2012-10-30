package br.ufmg.dcc.vod.spiderpig.jobs.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufmg.dcc.vod.spiderpig.master.Master;
import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.WorkerManager;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.stats.StatsActor;
import br.ufmg.dcc.vod.spiderpig.tracker.TrackerFactory;

public class TestWorkerInterested extends Master {

	private List<Integer> crawled;

	public TestWorkerInterested(TrackerFactory<String> trackerFactory,
			ProcessorActor processorActor, StatsActor statsActor,
			WorkerManager workerManager) {
		super(trackerFactory, processorActor, statsActor, workerManager);
		this.crawled = Collections.synchronizedList(new ArrayList<Integer>());
	}

	@Override
	public void crawlDone(CrawlID id, List<CrawlID> toQueue) {
		super.crawlDone(id, toQueue);
		this.crawled.add(Integer.parseInt(id.getId()));
	}
	
	public List<Integer> getCrawled() {
		return crawled;
	}
}
