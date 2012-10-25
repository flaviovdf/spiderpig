package br.ufmg.dcc.vod.ncrawler.master.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.jobs.WorkerInterested;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.WorkerID;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.WorkerManager;
import br.ufmg.dcc.vod.ncrawler.queue.QueueHandle;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.StringSerializer;
import br.ufmg.dcc.vod.ncrawler.queue.actor.AbstractActor;

/**
 * This abstract class defines a threaded processor, where {@value n} threads
 * will be started to dispatch jobs to be crawled. Each thread will dispatch
 * one job at a time.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class ProcessorActor extends AbstractActor<String> {

	private static final Logger LOG = 
			Logger.getLogger(ProcessorActor.class);
	
	private final WorkerManager manager;
	private final QueueHandle myHandle;
	
	protected final WorkerInterested interested;
	protected final FileSaver saver;

	public ProcessorActor(int numThreads, WorkerManager manager, 
			QueueService service, File queueFolder, int queueSize, 
			WorkerInterested interested, FileSaver saver) 
					throws FileNotFoundException, IOException {
		super(numThreads, service);
		this.manager = manager;
		this.interested = interested;
		this.saver = saver;
		this.myHandle = service.createPersistentMessageQueue("Workers", 
				queueFolder, new StringSerializer(), queueSize);
	}
	
	@Override
	public String getName() {
		return "CrawlProcessor";
	}

	@Override
	public void process(String crawlID) {
		try {
			WorkerID id = this.manager.allocateAvailableExecutor(crawlID);
			LOG.info("Dispacthing " + crawlID + " to worker " + id);
			id.resolve().crawl(crawlID, interested, saver);
		} catch (InterruptedException e) {
			LOG.error("Unexcpected interruption", e);
		}
	}

	@Override
	public QueueHandle getQueueHandle() {
		return myHandle;
	}
}