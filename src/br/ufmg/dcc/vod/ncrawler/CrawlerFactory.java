package br.ufmg.dcc.vod.ncrawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import br.ufmg.dcc.vod.ncrawler.common.ServiceIDUtils;
import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.RemoteMessageSender;
import br.ufmg.dcc.vod.ncrawler.distributed.worker.WorkerActor;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaverActor;
import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.master.DecoratorInterested;
import br.ufmg.dcc.vod.ncrawler.master.Master;
import br.ufmg.dcc.vod.ncrawler.master.ResultActor;
import br.ufmg.dcc.vod.ncrawler.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.MultiCoreManager;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.RemoteWorkerID;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.WorkerID;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.WorkerManager;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.WorkerManagerImpl;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.tracker.BloomFilterTrackerFactory;

/**
 * Contains auxiliary methods for creating default types of crawlers.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class CrawlerFactory {

	public static Crawler createThreadedCrawler(int numThreads,
			File queueDir, FileSaver saver, JobExecutor jobExecutor) 
					throws FileNotFoundException, IOException {
		
		QueueService service = new QueueService();
		BloomFilterTrackerFactory<String> trackerFactory = 
				new BloomFilterTrackerFactory<String>();
		
		DecoratorInterested workerInterested = new DecoratorInterested();
		WorkerManager workerManager = new MultiCoreManager(
				numThreads, jobExecutor);
		
		ProcessorActor processorActor = new ProcessorActor(workerManager, 
				service, workerInterested, saver);
		Master master = new Master(trackerFactory, processorActor, null,
				workerManager);
		
		processorActor.withFileQueue(service, queueDir);
		
		workerInterested.setLoopBack(master);
		
		return new ThreadedCrawler(processorActor, null, service,
				master, saver, numThreads);
	}
	
	public static Crawler createDistributedCrawler(String hostname, int port, 
			Set<InetSocketAddress> workerAddrs, File queueDir, FileSaver saver) 
					throws FileNotFoundException, IOException {
		
		QueueService service = new QueueService(hostname, port);
		BloomFilterTrackerFactory<String> trackerFactory = 
				new BloomFilterTrackerFactory<String>();

		int numThreads = workerAddrs.size();
		Set<WorkerID> workerIDs = new HashSet<>();
		
		ServiceID callBackID = ServiceIDUtils.toServiceID(hostname, port, 
				ResultActor.HANDLE);
		ServiceID fileSaverID = ServiceIDUtils.toServiceID(hostname, port, 
				FileSaverActor.HANDLE);
		RemoteMessageSender sender = new RemoteMessageSender();
		
		for (InetSocketAddress socketAddr : workerAddrs) {
			ServiceID workerID = ServiceIDUtils.toServiceID(
					socketAddr.getHostString(), socketAddr.getPort(), 
					WorkerActor.HANDLE);
			workerIDs.add(new RemoteWorkerID(workerID, callBackID, fileSaverID, 
					sender));
		}
		
		WorkerManager workerManager = new WorkerManagerImpl(workerIDs);
		
		ProcessorActor processorActor = new ProcessorActor(workerManager, 
				service, null, null);
		Master master = new Master(trackerFactory, processorActor, null, 
				workerManager);
		ResultActor resultActor = new ResultActor(master);
		FileSaverActor fileSaverActor = new FileSaverActor(saver);
		
		processorActor.withFileQueue(service, queueDir);
		fileSaverActor.withSimpleQueue(service);
		resultActor.withSimpleQueue(service);
		
		return new DistributedCrawler(processorActor, null, service, 
				master, resultActor, fileSaverActor, saver, numThreads);
	}
}
