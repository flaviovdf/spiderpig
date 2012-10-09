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

/**
 * This abstract class defines a threaded processor, where {@value n} threads
 * will be started to dispatch {@link CrawlJob} jobs. Each thread will dispatch
 * one job at a time.
 * 
 * TODO: Implement dynamic sleep time
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 *
 * @param <I> Type of IDs to evaluate
 * @param <C> Type of content being crawled
 */
public abstract class AbstractThreadedProcessor<I, C> 
		implements Processor<I, C> {

	private static final Logger LOG = 
			Logger.getLogger(AbstractThreadedProcessor.class);
	
	protected final long sleepTimePerExecution;
	
	private final int nThreads;
	private final QueueHandle myHandle;
	private final QueueService service;
	
	protected final Evaluator<I, C> eval;

	public AbstractThreadedProcessor(int nThreads, long sleepTimePerExecution, 
			QueueService service, Serializer<I> serializer, File queueFile, 
			int queueSize, Evaluator<I, C> eval) 
					throws FileNotFoundException, IOException {
		this.nThreads = nThreads;
		this.sleepTimePerExecution = sleepTimePerExecution;
		this.service = service;
		this.eval = eval;
		this.myHandle = service.createPersistentMessageQueue("Workers", 
				queueFile, serializer, queueSize);
	}
	
	public void start() {
		for (int i = 0; i < nThreads; i++) {
			service.startProcessor(myHandle, newQueueProcessor(i));
		}
		
		Collection<CrawlJob<I, C>> initialCrawl = eval.getInitialCrawl();
		for (CrawlJob<I, C> j : initialCrawl) {
			dispatch(j);
		}
	}

	@Override
	public void dispatch(CrawlJob<I, C> c) {
		try {
			service.sendObjectToQueue(myHandle, c);
		} catch (InterruptedException e) {
			LOG.error(e);
		}
	}
	
	public abstract QueueProcessor<?> newQueueProcessor(int i);
}
