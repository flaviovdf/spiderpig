package br.ufmg.dcc.vod.ncrawler;

import java.io.File;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.RandomizedSyncGraph;
import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.TestEvaluator;
import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.TestSerializer;

public class ThreadedCrawlerTest extends TestCase {

	private File myTempDir;

	@Before
	public void setUp() {
		String tmpDir = System.getProperty("java.io.tmpdir");
		do  {
			myTempDir = new File(tmpDir + File.separator + new Random().nextInt());
		} while (myTempDir.exists());
		
		myTempDir.mkdirs();
	}

	@After
	public void tearDown() {
		for (File f : myTempDir.listFiles()) {
			f.delete();
			f.deleteOnExit();
		}
		myTempDir.delete();
		myTempDir.deleteOnExit();
	}
	
	@Test
	public void testCrawl1Thread() throws Exception {
		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
		
		TestEvaluator te = new TestEvaluator(g);
		ThreadedCrawler tc = new ThreadedCrawler(1, 0, te, myTempDir, new TestSerializer(g) ,1024 * 1024);
		
		tc.crawl();
		
		Map<Integer, int[]> crawled = te.getCrawled();
		doTheAsserts(crawled, g);
	}


	@Test
	public void testCrawl2Thread() throws Exception {
		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
		
		TestEvaluator te = new TestEvaluator(g);
		ThreadedCrawler tc = new ThreadedCrawler(1, 0, te, myTempDir, new TestSerializer(g) ,1024 * 1024);
		
		tc.crawl();
		
		Map<Integer, int[]> crawled = te.getCrawled();
		doTheAsserts(crawled, g);
	}
	
	@Test
	public void testCrawl100Thread() throws Exception {
		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
		
		TestEvaluator te = new TestEvaluator(g);
		ThreadedCrawler tc = new ThreadedCrawler(100, 0, te, myTempDir,new TestSerializer(g) ,1024 * 1024);
		
		tc.crawl();
		
		Map<Integer, int[]> crawled = te.getCrawled();
		doTheAsserts(crawled, g);
	}

	
	private void doTheAsserts(Map<Integer, int[]> crawled, RandomizedSyncGraph g) {
		assertEquals(crawled.size(), g.getNumVertex());
		
		for (Entry<Integer, int[]> e: crawled.entrySet()) {
			
			int[] neighbours = g.getNeighbours(e.getKey());
			int[] value = e.getValue();
			
			assertEquals(neighbours.length, value.length);
			for (int i = 0; i < neighbours.length; i++) {
				assertEquals(neighbours[i], value[i]);
			}
		}
	}
}