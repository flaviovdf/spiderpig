package br.ufmg.dcc.vod.spiderpig;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.FDClientActor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaverActor;
import br.ufmg.dcc.vod.spiderpig.master.Master;
import br.ufmg.dcc.vod.spiderpig.master.ResultActor;
import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.CrawlFinishedListener;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;

public class Crawler {

	private static final Logger LOG = Logger.getLogger(Crawler.class);
	
	private final QueueService service;
	private final ProcessorActor processorActor;
	private final Master master;
	private final StopCondition stopCondition;
	private final int numThreads;

	private final ResultActor resultActor;
	private final FileSaverActor fileSaverActor;
	private final FDClientActor fd;
	
	Crawler(ProcessorActor processorActor, 
			QueueService service, Master master, 
			ResultActor resultActor, FileSaverActor fileSaverActor, 
			FDClientActor fd, int numThreads) {
		this.processorActor = processorActor;
		this.service = service;
		this.master = master;
		this.stopCondition = master.getStopCondition();
		this.numThreads = numThreads;
		this.resultActor = resultActor;
		this.fileSaverActor = fileSaverActor;
		this.fd = fd;
	}
	
	public void addSeed(Iterable<String> seeds) {
		master.setSeeds(seeds);
	}

	public void addSeed(File seedFile) throws IOException {
		master.setSeeds(seedFile);
	}
	
	public void addSeed(String... seeds) {
		master.setSeeds(Arrays.asList(seeds));
	}
	
	public void crawl() {
		LOG.info("Starting Crawler");

		//Starting Processors
		resultActor.startProcessors(numThreads);
		fileSaverActor.startProcessors(numThreads);
		processorActor.startProcessors(numThreads);
		fd.startProcessors(1);
		fd.startTimer();
		
		//Waiting until crawl ends
		LOG.info("Waiting until crawl ends");
		
		final CountDownLatch latch = new CountDownLatch(1);
		stopCondition.addCrawlFinishedListener(new CrawlFinishedListener() {
			@Override
			public void crawlDone() {
				latch.countDown();
			}
		});
		
		try {
			latch.await();
			fd.stopTimer();
			service.waitUntilWorkIsDoneAndStop(1);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		try {
			this.fileSaverActor.closeSaver();
		} catch (IOException e) {
			LOG.warn("Unable to shutdown file saver ", e);
		}
		LOG.info("Crawl done!");
	}
}