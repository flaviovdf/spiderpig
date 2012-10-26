package br.ufmg.dcc.vod.ncrawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import br.ufmg.dcc.vod.ncrawler.distributed.filesaver.UploadListener;
import br.ufmg.dcc.vod.ncrawler.distributed.master.ResultListener;
import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.NIOServer;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.master.LoopBackInterested;
import br.ufmg.dcc.vod.ncrawler.master.Master;
import br.ufmg.dcc.vod.ncrawler.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.MultiCoreManager;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.RemoteWorkerID;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.WorkerID;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.WorkerManager;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.WorkerManagerImpl;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Payload.UploadMessage;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Worker.BaseResult;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.tracker.BloomFilterTrackerFactory;

/**
 * Contains auxiliary methods for creating default types of crawlers.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class CrawlerFactory {

	private static final int DEFAULT_QUEUE_SIZE = 1024 * 1024;
	
	public static Crawler createThreadedCrawler(int numThreads,
			File queueDir, FileSaver saver, JobExecutor jobExecutor) 
					throws FileNotFoundException, IOException {
		
		QueueService service = new QueueService();
		BloomFilterTrackerFactory<String> trackerFactory = 
				new BloomFilterTrackerFactory<String>();
		
		LoopBackInterested workerInterested = new LoopBackInterested();
		WorkerManager workerManager = new MultiCoreManager(
				numThreads, jobExecutor);
		
		ProcessorActor processorActor = new ProcessorActor(numThreads, 
				workerManager, service, queueDir, DEFAULT_QUEUE_SIZE, 
				workerInterested, saver);
		Master master = new Master(trackerFactory, processorActor, null,
				workerManager);
		
		workerInterested.setLoopBack(master);
		
		return new ThreadedCrawler(processorActor, null, service,
				master, saver);
	}
	
	public static Crawler createDistributedCrawler(String callBackHost, 
			int callBackPort, String fileSaverHost, int fileSaverPort,
			Set<InetSocketAddress> workerAddrs, File queueDir, FileSaver saver) 
					throws FileNotFoundException, IOException {
		
		QueueService service = new QueueService();
		BloomFilterTrackerFactory<String> trackerFactory = 
				new BloomFilterTrackerFactory<String>();

		int numThreads = workerAddrs.size();
		Set<WorkerID> workerIDs = new HashSet<>();
		
		for (InetSocketAddress socketAddr : workerAddrs)
			workerIDs.add(new RemoteWorkerID(socketAddr.getHostString(),
					socketAddr.getPort(), callBackHost, callBackPort, 
					fileSaverHost, fileSaverPort));
		
		WorkerManager workerManager = new WorkerManagerImpl(workerIDs);
		
		ProcessorActor processorActor = new ProcessorActor(numThreads, 
				workerManager, service, queueDir, DEFAULT_QUEUE_SIZE, null, 
				null);
		Master master = new Master(trackerFactory, processorActor, null, 
				workerManager);
		
		ResultListener resultListener = new ResultListener(master);
		NIOServer<BaseResult> resultServer = new NIOServer<>(numThreads, 
				callBackHost, callBackPort, resultListener);
		
		UploadListener uploadListener = new UploadListener(saver);
		NIOServer<UploadMessage> fileServer = new NIOServer<>(-1, 
				fileSaverHost, fileSaverPort, uploadListener);
		return new DistributedCrawler(processorActor, null, service, 
				master, resultServer, fileServer, saver);
	}
}
