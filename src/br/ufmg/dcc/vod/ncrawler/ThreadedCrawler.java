package br.ufmg.dcc.vod.ncrawler;

import java.util.List;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.stats.StatsActor;

import com.google.common.base.Preconditions;

public class ThreadedCrawler implements Crawler {

	private static final Logger LOG = Logger.getLogger(ThreadedCrawler.class);
	
	private final StatsActor statsActor;
	private final QueueService service;
	private final ProcessorActor processorActor;

	protected ThreadedCrawler(ProcessorActor processorActor, 
			StatsActor statsActor, QueueService service) {
		Preconditions.checkNotNull(processorActor);
		Preconditions.checkNotNull(service);
		
		this.processorActor = processorActor;
		this.statsActor = statsActor;
		this.service = service;
	}
	
	public void dispatch(List<String> seed) {
		for (String crawlID : seed)
			this.processorActor.dispatch(crawlID);
	}

	public void dispatch(String... seed) {
		for (String crawlID : seed)
			this.processorActor.dispatch(crawlID);
	}
	
	public void crawl() {
		LOG.info("Starting Crawler");

		//Starting
		if (statsActor != null)
			statsActor.start();
		processorActor.start();
		
		//Waiting until crawl ends
		int wi = 10;
		LOG.info("Waiting until crawl ends: waitInterval="+wi+"s");
		this.service.waitUntilWorkIsDoneAndStop(wi);
		
		LOG.info("Done! Stopping");
		System.out.println("Done! Stopping");
		LOG.info("Crawl done!");
	}
}