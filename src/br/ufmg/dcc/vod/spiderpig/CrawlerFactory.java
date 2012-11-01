package br.ufmg.dcc.vod.spiderpig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import br.ufmg.dcc.vod.spiderpig.common.ServiceIDUtils;
import br.ufmg.dcc.vod.spiderpig.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.FDClientActor;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.FDServerActor;
import br.ufmg.dcc.vod.spiderpig.distributed.worker.WorkerActor;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaverActor;
import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;
import br.ufmg.dcc.vod.spiderpig.master.DecoratorInterested;
import br.ufmg.dcc.vod.spiderpig.master.Master;
import br.ufmg.dcc.vod.spiderpig.master.ResultActor;
import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.LoopbackResolver;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.MultiCoreManager;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.RemoteResolver;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.Resolver;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.WorkerManager;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.WorkerManagerImpl;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.tracker.BloomFilterTrackerFactory;

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
		WorkerManager workerManager = new MultiCoreManager(numThreads);
		
		Resolver resolver = new LoopbackResolver(jobExecutor);
		ProcessorActor processorActor = new ProcessorActor(workerManager, 
				service, resolver, workerInterested, saver);
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
		Set<ServiceID> workerIDs = new HashSet<>();
		
		ServiceID callBackID = ServiceIDUtils.
				toResolvedServiceID(hostname, port, ResultActor.HANDLE);
		ServiceID fileSaverID = ServiceIDUtils.
				toResolvedServiceID(hostname, port, FileSaverActor.HANDLE);
		
		for (InetSocketAddress socketAddr : workerAddrs) {
			ServiceID workerID = 
					ServiceIDUtils.toResolvedServiceID(
					socketAddr.getHostName(), 
					socketAddr.getPort(), WorkerActor.HANDLE);
			workerIDs.add(workerID);
		}
		
		RemoteMessageSender sender = new RemoteMessageSender();
		WorkerManager workerManager = new WorkerManagerImpl(workerIDs);
		Resolver resolver = new RemoteResolver(callBackID, fileSaverID, sender);
		ProcessorActor processorActor = new ProcessorActor(workerManager, 
				service, resolver, null, null);
		Master master = new Master(trackerFactory, processorActor, null, 
				workerManager);
		
		ResultActor resultActor = new ResultActor(master);
		FileSaverActor fileSaverActor = new FileSaverActor(saver);
		FDClientActor fd = new FDClientActor(30, 5, TimeUnit.SECONDS, 
				master, sender);
		
		fd.withSimpleQueue(service);
		processorActor.withFileQueue(service, queueDir);
		fileSaverActor.withSimpleQueue(service);
		resultActor.withSimpleQueue(service);
		
		for (ServiceID sid : workerIDs)
			fd.watch(
					ServiceIDUtils
					.toResolvedServiceID(sid.getHostname(), sid.getPort(), 
					 FDServerActor.HANDLE));
		
		return new DistributedCrawler(processorActor, null, service, 
				master, resultActor, fileSaverActor, fd, 
				saver, numThreads);
	}
}
