package br.ufmg.dcc.vod.ncrawler.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.queue.QueueHandle;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public abstract class AbstractProcessor implements Processor {

	private static final Logger LOG = Logger.getLogger(AbstractProcessor.class);
	
	protected final long sleepTimePerExecution;
	
	private final int nThreads;
	private final QueueHandle myHandle;
	private final QueueService service;
	
	@SuppressWarnings("unchecked")
	protected final Evaluator eval;

	public <S, I, C> AbstractProcessor(int nThreads, long sleepTimePerExecution, QueueService service,
			Serializer<S> serializer, File queueFile, int queueSize, Evaluator<I, C> eval) 
			throws FileNotFoundException, IOException {
		this.nThreads = nThreads;
		this.sleepTimePerExecution = sleepTimePerExecution;
		this.service = service;
		this.eval = eval;
		this.myHandle = service.createPersistentMessageQueue("Workers", queueFile, serializer, queueSize);
	}
	
	public void start() {
		for (int i = 0; i < nThreads; i++) {
			service.startProcessor(myHandle, newQueueProcessor(i));
		}
		
		Collection<CrawlJob> initialCrawl = eval.getInitialCrawl();
		for (CrawlJob j : initialCrawl) {
			dispatch(j);
		}
	}

	@Override
	public void dispatch(CrawlJob c) {
		try {
			service.sendObjectToQueue(myHandle, c);
		} catch (InterruptedException e) {
			LOG.error(e);
		}
	}
	
	public abstract QueueProcessor<?> newQueueProcessor(int i);
}
