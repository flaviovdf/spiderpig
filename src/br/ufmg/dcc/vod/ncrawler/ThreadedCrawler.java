package br.ufmg.dcc.vod.ncrawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.ThreadSafeEvaluator;
import br.ufmg.dcc.vod.ncrawler.processor.ThreadedProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;

public class ThreadedCrawler {

	private static final Logger LOG = Logger.getLogger(ThreadedCrawler.class);
	
	private final ThreadedProcessor processor;
	private final QueueService service;
	
	private final int nThreads;
	private final long sleep;

	private final StatsPrinter sp;
	private final ThreadSafeEvaluator eval;

	public <S> ThreadedCrawler(int nThreads, long sleep, Evaluator evaluator, File pQueueDir, Serializer<S> s, int fileSize) 
		throws FileNotFoundException, IOException {
		
		this.nThreads = nThreads;
		this.sleep = sleep;
		this.service = new QueueService();
		this.sp = new StatsPrinter(service);
		this.eval = new ThreadSafeEvaluator(nThreads, evaluator, service);
		this.processor = new ThreadedProcessor(nThreads, sleep, service, s, pQueueDir, fileSize, eval);
	}
	
	public void crawl() throws Exception {
		LOG.info("Starting ThreadedCrawler: nThreads="+nThreads + " , sleepTime="+sleep+"s");

		eval.setProcessor(processor);
		eval.setStatsKeeper(sp);
		
		//Starting
		sp.start();
		processor.start();
		eval.start();
		
		//Waiting until crawl ends
		int wi = 10;
		LOG.info("Waiting until crawl ends: waitInterval="+wi+"s");
		this.service.waitUntilWorkIsDoneAndStop(wi);

		LOG.info("Done! Stopping");
		System.out.println("Done! Stopping");
		LOG.info("Crawl done!");
	}
}