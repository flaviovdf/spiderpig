package br.ufmg.dcc.vod.spiderpig.master.processor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;

import br.ufmg.dcc.vod.spiderpig.common.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.common.queue.serializer.MessageLiteSerializer;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.Resolver;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.WorkerManager;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

/**
 * A processor is responsible for dispatching {@link CrawlID}s or getting
 * cached results for speed-ups.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class ProcessorActor extends Actor<CrawlID> 
		implements QueueProcessor<CrawlID> {

	private static final String HANDLE = "Processor";

	private static final Logger LOG = 
			Logger.getLogger(ProcessorActor.class);
	
	private final WorkerManager manager;
	private final Resolver resolver;
	private final WorkerInterested interested;
	private final FileSaver saver;
	private final Cache<CrawlID, List<CrawlID>> resultsCache;


	public ProcessorActor(WorkerManager manager, QueueService service, 
			Resolver resolver, WorkerInterested interested, FileSaver saver,
			Cache<CrawlID, List<CrawlID>> resultsCache) 
					throws FileNotFoundException, IOException {
		super(HANDLE);
		this.manager = manager;
		this.resolver = resolver;
		this.interested = interested;
		this.saver = saver;
		this.resultsCache = resultsCache;
	}
	
	@Override
	public void process(CrawlID crawlID) {
		boolean inCache = false;
		if (resultsCache != null) {
			List<CrawlID> cached = resultsCache.getIfPresent(crawlID);
			if (cached != null) {
				interested.crawlDone(crawlID, cached);
				inCache = true;
				LOG.info("Found " + crawlID + " in cache");
			}
		}
		
		if (!inCache) {
			try {
				ServiceID sid = this.manager.allocateAvailableExecutor(crawlID);
				LOG.info("Sending " + crawlID + " to worker " + sid);
				resolver.resolve(sid).crawl(crawlID, interested, saver);
			} catch (InterruptedException e) {
				LOG.error("Unexcpected interruption", e);
			}
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