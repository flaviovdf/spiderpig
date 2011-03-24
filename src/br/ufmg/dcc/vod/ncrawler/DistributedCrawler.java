package br.ufmg.dcc.vod.ncrawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.DistributedProcessor;
import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.EvaluatorClientImpl;
import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.ServerID;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.ThreadSafeEvaluator;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;

public class DistributedCrawler {

	private static final Logger LOG = Logger.getLogger(DistributedCrawler.class);
	
	private final DistributedProcessor processor;
	private final QueueService service;
	
	private final long sleep;

	private final StatsPrinter sp;
	private final ThreadSafeEvaluator tEval;
	private final Set<ServerID> workers;
	private final EvaluatorClientImpl evaluatorClient;

	public <S> DistributedCrawler(Set<ServerID> workers, long sleep, EvaluatorClientImpl evaluatorClient, 
			Evaluator evaluator, File pQueueDir, Serializer<S> s, int fileSize) 
		throws FileNotFoundException, IOException {
		
		this.workers =  workers;
		this.sleep = sleep;
		this.service = new QueueService();
		
		this.tEval = new ThreadSafeEvaluator(workers.size(), evaluator, service);
		this.evaluatorClient = evaluatorClient;
		this.evaluatorClient.wrap(tEval);
		
		this.processor = new DistributedProcessor(sleep, service, s, pQueueDir, fileSize, workers, evaluator, evaluatorClient);
		this.sp = new StatsPrinter(service);
	}
	
	public void crawl() throws Exception {
		LOG.info("Starting Distributed: nWorkers=" + workers.size() + " , sleepTime="+sleep+"s");

		tEval.setProcessor(processor);
		tEval.setStatsKeeper(sp);
		
		//Starting
		sp.start();
		processor.start();
		tEval.start();
		
		//Waiting until crawl ends
		int wi = 10;
		LOG.info("Waiting until crawl ends: waitInterval="+wi+"s");
		this.service.waitUntilWorkIsDoneAndStop(wi);

		LOG.info("Done! Stopping");
		System.out.println("Done! Stopping");
		LOG.info("Crawl done!");
	}
}
