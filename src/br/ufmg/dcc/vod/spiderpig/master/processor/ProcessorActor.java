package br.ufmg.dcc.vod.spiderpig.master.processor;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.Resolver;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.WorkerManager;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.queue.serializer.MessageLiteSerializer;

/**
 * This abstract class defines a threaded processor, where {@value n} threads
 * will be started to dispatch jobs to be crawled. Each thread will dispatch
 * one job at a time.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class ProcessorActor extends Actor<CrawlID> 
		implements QueueProcessor<CrawlID> {

	private static final String HANDLE = "Processor";

	private static final Logger LOG = 
			Logger.getLogger(ProcessorActor.class);
	
	private final WorkerManager manager;
	private final WorkerInterested interested;
	private final FileSaver saver;

	public ProcessorActor(WorkerManager manager, QueueService service, 
			WorkerInterested interested, FileSaver saver) 
					throws FileNotFoundException, IOException {
		super(HANDLE);
		this.manager = manager;
		this.interested = interested;
		this.saver = saver;
	}
	
	@Override
	public void process(CrawlID crawlID) {
		try {
			Resolver res = this.manager.allocateAvailableExecutor(crawlID);
			LOG.info("Sending " + crawlID + " to worker " + res.getWorkerID());
			res.resolve().crawl(crawlID, interested, saver);
		} catch (InterruptedException e) {
			LOG.error("Unexcpected interruption", e);
		}
	}

	@Override
	public QueueProcessor<CrawlID> getQueueProcessor() {
		return this;
	}

	@Override
	public MessageLiteSerializer<CrawlID> newMsgSerializer() {
		return new MessageLiteSerializer<>(CrawlID.newBuilder());
	}
}