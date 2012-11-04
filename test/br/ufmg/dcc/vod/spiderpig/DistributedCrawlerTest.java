package br.ufmg.dcc.vod.spiderpig;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.ufmg.dcc.vod.spiderpig.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.FDServerActor;
import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;
import br.ufmg.dcc.vod.spiderpig.jobs.test.RandomizedSyncGraph;
import br.ufmg.dcc.vod.spiderpig.jobs.test.TestFileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.test.TestJobExecutor;
import br.ufmg.dcc.vod.spiderpig.master.walker.BFSWalker;
import br.ufmg.dcc.vod.spiderpig.master.walker.Walker;
import br.ufmg.dcc.vod.spiderpig.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.worker.WorkerActor;

public class DistributedCrawlerTest  extends TestCase {

	private File myTempDir;
	private ArrayList<QueueService> workerServices;

	@Before
	public void setUp() throws Exception {
		String tmpDir = System.getProperty("java.io.tmpdir");
		do  {
			myTempDir = new File(tmpDir + File.separator + new Random().nextInt());
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
			RandomizedSyncGraph g, int basePort) throws IOException {
		Set<InetSocketAddress> ids = new HashSet<>();
		RemoteMessageSender sender = new RemoteMessageSender();
		for (int i = 0; i < numServers; i++) {
			JobExecutor jobExecutor = new TestJobExecutor(g);
			QueueService service = new QueueService("localhost", basePort + i);
			
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
		
		TestFileSaver saver = new TestFileSaver();
		
		String host = "localhost";
		Walker walker = BFSWalker.getTestWalker();
		Crawler crawler = 
				CrawlerFactory.createDistributedCrawler(host, 4541, 
						workerAddrs, myTempDir, saver, walker, null);
		
		crawler.dispatch("0");
		crawler.crawl();
		
		Map<Integer, byte[]> crawled = saver.getCrawled();
		doTheAsserts(crawled, g);
	}


	@Test
	public void testCrawl2Thread() throws Exception {
		
		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
		Set<InetSocketAddress> workerAddrs = initiateServers(2, g, 6000);
		
		TestFileSaver saver = new TestFileSaver();
		
		String host = "localhost";
		Walker walker = BFSWalker.getTestWalker();
		Crawler crawler = 
				CrawlerFactory.createDistributedCrawler(host, 4542, 
						workerAddrs, myTempDir, saver, walker, null);
		
		crawler.dispatch("0");
		crawler.crawl();
		
		Map<Integer, byte[]> crawled = saver.getCrawled();
		doTheAsserts(crawled, g);
	}
	
	@Test
	public void testCrawl100Thread() throws Exception {
		
		RandomizedSyncGraph g = new RandomizedSyncGraph(200);
		Set<InetSocketAddress> workerAddrs = initiateServers(100, g, 7000);
		
		TestFileSaver saver = new TestFileSaver();
		
		String host = "localhost";
		Walker walker = BFSWalker.getTestWalker();
		Crawler crawler = 
				CrawlerFactory.createDistributedCrawler(host, 4543, 
						workerAddrs, myTempDir, saver, walker, null);
		
		crawler.dispatch("0");
		crawler.crawl();
		
		Map<Integer, byte[]> crawled = saver.getCrawled();
		doTheAsserts(crawled, g);
	}

	
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