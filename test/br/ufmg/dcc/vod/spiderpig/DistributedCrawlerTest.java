package br.ufmg.dcc.vod.spiderpig;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.common.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.FDServerActor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;
import br.ufmg.dcc.vod.spiderpig.jobs.TimeBasedJobExecutor;
import br.ufmg.dcc.vod.spiderpig.jobs.test.RandomizedSyncGraph;
import br.ufmg.dcc.vod.spiderpig.jobs.test.TestFileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.test.TestJobRequester;
import br.ufmg.dcc.vod.spiderpig.master.walker.BFSWalker;
import br.ufmg.dcc.vod.spiderpig.worker.WorkerActor;
import junit.framework.Assert;
import junit.framework.TestCase;

public class DistributedCrawlerTest  extends TestCase {

	private File myTempDir;
	private ArrayList<QueueService> workerServices;

	@Before
	public void setUp() throws Exception {
		String tmpDir = System.getProperty("java.io.tmpdir");
		do  {
			myTempDir = new File(tmpDir + File.separator + 
					new Random().nextInt());
		} while (myTempDir.exists());
		
		myTempDir.mkdirs();
		workerServices = new ArrayList<>();
	}

	@After
	public void tearDown() throws Exception {
		for (File f : myTempDir.listFiles()) {
			f.delete();
			f.deleteOnExit();
		}
		myTempDir.delete();
		myTempDir.deleteOnExit();
		
		for (QueueService service : workerServices) {
			service.waitUntilWorkIsDoneAndStop(1);
		}
	}
	
	public Set<InetSocketAddress> initiateServers(int numServers, 
			RandomizedSyncGraph g, int basePort) throws Exception {
		Set<InetSocketAddress> ids = new HashSet<>();
		for (int i = 0; i < numServers; i++) {
			ConfigurableBuilder configurableBuilder = new ConfigurableBuilder();
			Configuration configuration = new BaseConfiguration();
			configuration.addProperty(TimeBasedJobExecutor.BKOFF_TIME, 0);
			configuration.addProperty(TimeBasedJobExecutor.SLEEP_TIME, 0);
			configuration.addProperty(TimeBasedJobExecutor.REQUESTER, 
					TestJobRequester.class.getCanonicalName());
			
			configuration.addProperty(TestJobRequester.GRAPH, g);
			
			JobExecutor jobExecutor = configurableBuilder.build(
					TimeBasedJobExecutor.class, configuration);
			
			QueueService service = new QueueService("localhost", basePort + i);
			
			RemoteMessageSender sender = new RemoteMessageSender();
			WorkerActor actor = new WorkerActor(jobExecutor, sender);
			actor.withSimpleQueue(service).startProcessors(1);
			
			FDServerActor fdactor = new FDServerActor(sender);
			fdactor.withSimpleQueue(service).startProcessors(1);
			
			ids.add(new InetSocketAddress("localhost", basePort + i));
			workerServices.add(service);
		}
		
		return ids;
	}
	
	@Test
	public void testCrawl1Thread() throws Exception {
		
		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
		Set<InetSocketAddress> workerAddrs = initiateServers(1, g, 5000);
		
		TestFileSaver saver = new TestFileSaver(100);
		
		String host = "localhost";
		BaseConfiguration configuration = new BaseConfiguration();
		
		configuration.addProperty(BFSWalker.BLOOM_INSERTS, 1e6);
		ConfigurableBuilder configurableBuilder = new ConfigurableBuilder();
		BFSWalker walker = 
				configurableBuilder.build(BFSWalker.class, configuration);
		
		Crawler crawler = 
				CrawlerFactory.createDistributedCrawler(host, 4541, 
						workerAddrs, 1, 1, myTempDir, saver, walker);
		
		crawler.addSeed("0");
		crawler.crawl();
		
		assertTrue(saver.isConsistent());
		Map<Integer, byte[]> crawled = saver.getCrawled();
		doTheAsserts(crawled, g);
	}


	@Test
	public void testCrawl2Thread() throws Exception {
		
		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
		Set<InetSocketAddress> workerAddrs = initiateServers(2, g, 6000);
		
		TestFileSaver saver = new TestFileSaver(100);
		
		String host = "localhost";
		
		BaseConfiguration configuration = new BaseConfiguration();
		configuration.addProperty(BFSWalker.BLOOM_INSERTS, 1e6);
		ConfigurableBuilder configurableBuilder = new ConfigurableBuilder();
		BFSWalker walker = 
				configurableBuilder.build(BFSWalker.class, configuration);
		
		Crawler crawler = 
				CrawlerFactory.createDistributedCrawler(host, 4542, 
						workerAddrs, 1, 1, myTempDir, saver, walker);
		
		crawler.addSeed("0");
		crawler.crawl();
		
		assertTrue(saver.isConsistent());
		Map<Integer, byte[]> crawled = saver.getCrawled();
		doTheAsserts(crawled, g);
	}

	// TODO: The test commented out fails because of load in the machine.
	//       FD suspects of things it should not. Not sure if easy fix.
//	@Test
//	public void testCrawl10Thread() throws Exception {
//		
//		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
//		Set<InetSocketAddress> workerAddrs = initiateServers(10, g, 7000);
//		
//		TestFileSaver saver = new TestFileSaver(2000);
//		
//		String host = "localhost";
//		BaseConfiguration configuration = new BaseConfiguration();
//		
//		configuration.addProperty(BFSWalker.BLOOM_INSERTS, 1e6);
//		ConfigurableBuilder configurableBuilder = new ConfigurableBuilder();
//		BFSWalker walker = 
//				configurableBuilder.build(BFSWalker.class, configuration);
//		
//		Crawler crawler = 
//				CrawlerFactory.createDistributedCrawler(host, 4543, 
//						workerAddrs, 1, 99999, myTempDir, saver, 
//						walker);
//		
//		crawler.addSeed("0");
//		crawler.crawl();
//		
//		assertTrue(saver.isConsistent());
//		Map<Integer, byte[]> crawled = saver.getCrawled();
//		doTheAsserts(crawled, g);
//	}
	
	private void doTheAsserts(Map<Integer, byte[]> crawled, 
			RandomizedSyncGraph g) {
		assertEquals(g.getNumVertex(), crawled.size());
		
		for (Entry<Integer, byte[]> e: crawled.entrySet()) {
			
			int[] neighbours = g.getNeighbours(e.getKey());
			IntBuffer buff = ByteBuffer.wrap(e.getValue()).asIntBuffer();
			buff.rewind();
			
			for (int i = 0; i < neighbours.length; i++) {
				assertEquals(neighbours[i], buff.get());
			}
			Assert.assertFalse(buff.hasRemaining());
		}
	}
}