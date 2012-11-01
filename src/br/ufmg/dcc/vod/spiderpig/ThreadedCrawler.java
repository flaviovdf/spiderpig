package br.ufmg.dcc.vod.spiderpig;

import java.util.List;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.master.Master;
import br.ufmg.dcc.vod.spiderpig.master.StopCondition;
import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.stats.StatsActor;

import com.google.common.base.Preconditions;

public class ThreadedCrawler implements Crawler {

	private static final Logger LOG = Logger.getLogger(ThreadedCrawler.class);
	
	protected final StatsActor statsActor;
	protected final QueueService service;
	protected final ProcessorActor processorActor;
	protected final Master master;
	protected final StopCondition stopCondition;
	protected final FileSaver saver;
	protected final int numThreads;


	protected ThreadedCrawler(ProcessorActor processorActor, 
			StatsActor statsActor, QueueService service, 
			Master master, FileSaver saver, int numThreads) {
		Preconditions.checkNotNull(processorActor);
		Preconditions.checkNotNull(service);
		
		this.processorActor = processorActor;
		this.statsActor = statsActor;
		this.service = service;
		this.master = master;
		this.saver = saver;
		this.stopCondition = master.getStopCondition();
		this.numThreads = numThreads;
	}
	
	public void dispatch(List<String> seed) {
		CrawlID.Builder builder = CrawlID.newBuilder();
		for (String crawlID : seed)
			this.master.dispatch(builder.setId(crawlID).build());
	}

	public void dispatch(String... seed) {
		CrawlID.Builder builder = CrawlID.newBuilder();
		for (String crawlID : seed)
			this.master.dispatch(builder.setId(crawlID).build());
	}
	
	public void crawl() {
		System.out.println("Starting Crawler");
		LOG.info("Starting Crawler");

		//Starting
		if (statsActor != null)
			statsActor.startProcessors(1);
		processorActor.startProcessors(numThreads);
		
		//Waiting until crawl ends
		LOG.info("Waiting until crawl ends");
		this.stopCondition.awaitAllDone();
		this.service.waitUntilWorkIsDoneAndStop(1);
		
		LOG.info("Done! Stopping");
		System.out.println("Done! Stopping");
		LOG.info("Crawl done!");
	}
}