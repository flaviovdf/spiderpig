package br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.master.Master;
import br.ufmg.dcc.vod.ncrawler.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.WorkerManager;
import br.ufmg.dcc.vod.ncrawler.stats.StatsActor;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

public class TestWorkerInterested extends Master {

	private List<Integer> crawled;

	public TestWorkerInterested(TrackerFactory<String> trackerFactory,
			ProcessorActor processorActor, StatsActor statsActor,
			WorkerManager workerManager) {
		super(trackerFactory, processorActor, statsActor, workerManager);
		this.crawled = Collections.synchronizedList(new ArrayList<Integer>());
	}

	@Override
	public void crawlDone(String id, List<String> toQueue) {
		super.crawlDone(id, toQueue);
		this.crawled.add(Integer.parseInt(id));
	}
	
	public List<Integer> getCrawled() {
		return crawled;
	}
}
