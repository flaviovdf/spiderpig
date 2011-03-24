package br.ufmg.dcc.vod.ncrawler.queue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultiFileMMapFifoQueueTest extends TestCase {

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
	public void testQueuePutErrors() throws FileNotFoundException, IOException {
		SS ss = new SS();
		MultiFileMMapFifoQueue<String> q = new MultiFileMMapFifoQueue<String>(myTempDir, ss, 1024 * 1024);
		
		try {
			q.take();
			fail();
		} catch (QueueServiceException e) {
		}
	}
	
	@Test
	public void testQueuePutGet() throws FileNotFoundException, IOException {
		SS ss = new SS();
		MultiFileMMapFifoQueue<String> q = new MultiFileMMapFifoQueue<String>(myTempDir, ss, 1024 * 1024);
		
		assertEquals(0, q.size());
		assertEquals(1, myTempDir.listFiles().length);
		
		q.put("a");
		
		assertEquals(1, myTempDir.listFiles().length);
		assertEquals(1, q.size());
		
		assertEquals("a", q.take());
		assertEquals(0, q.size());
		
		assertEquals(1, myTempDir.listFiles().length);
	}

	@Test
	public void testQueuePutGet2() throws FileNotFoundException, IOException {
		SS ss = new SS();
		MultiFileMMapFifoQueue<String> q = new MultiFileMMapFifoQueue<String>(myTempDir, ss, 15);
		q.put("a"); //This will succeed!
		assertEquals(2, myTempDir.listFiles().length);
		assertEquals(1, q.size()); //we cannot know the size prefold, since the limit is low each object will be on
		//a file
		
		q.put("b"); //So will this
		assertEquals(3, myTempDir.listFiles().length);
		assertEquals(2, q.size());
		
		q.put("c");
		assertEquals(4, myTempDir.listFiles().length);
		assertEquals(3, q.size());

		assertEquals("a", q.take());
		assertEquals("b", q.take());
		assertEquals("c", q.take());
		
		assertEquals(4, myTempDir.listFiles().length);		
		assertEquals(0, q.size());
		
		try {
			q.take();
			fail();
		} catch (QueueServiceException e) {}
		
		q.put("a");
		assertEquals(5, myTempDir.listFiles().length);		
		assertEquals(1, q.size());
	}
	
	@Test
	public void testQueuePutGet3() throws FileNotFoundException, IOException {
		SS ss = new SS();
		MultiFileMMapFifoQueue<String> q = new MultiFileMMapFifoQueue<String>(myTempDir, ss, 1024 * 1024);
		assertEquals(1, myTempDir.listFiles().length);
		
		assertEquals(0, q.size());
		q.put("a");
		assertEquals(1, q.size());
		
		q.put("b");
		assertEquals(2, q.size());
		
		q.put("c");
		assertEquals(3, q.size());
		
		q.put("d");
		assertEquals(4, q.size());
		
		q.put("e");
		assertEquals(5, q.size());
		
		assertEquals("a", q.take());
		assertEquals(4, q.size());
		assertEquals("b", q.take());
		assertEquals(3, q.size());
		assertEquals("c", q.take());
		assertEquals(2, q.size());
		assertEquals("d", q.take());
		assertEquals(1, q.size());
		assertEquals("e", q.take());
		assertEquals(0, q.size());
		
		assertEquals(1, myTempDir.listFiles().length);
	}
	
	@Test
	public void testQueuePutGet4() throws FileNotFoundException, IOException {
		SS ss = new SS();
		MultiFileMMapFifoQueue<String> q = new MultiFileMMapFifoQueue<String>(myTempDir, ss, 1024 * 1024);
		
		try {
			q.take();
			fail();
		} catch (QueueServiceException e) {}
		
		assertEquals(0, q.size());
		q.put("a");
		assertEquals(1, q.size());
		assertEquals("a", q.take());
		assertEquals(0, q.size());
		
		try {
			q.take();
			fail();
		} catch (QueueServiceException e) {}
	}
	
	@Test
	public void testQueuePutGet5() throws FileNotFoundException, IOException {
		SS ss = new SS();
		MultiFileMMapFifoQueue<String> q = new MultiFileMMapFifoQueue<String>(myTempDir, ss, 1024 * 1024);
		
		assertEquals(0, q.size());
		q.put("a");
		assertEquals(1, q.size());
		
		q.put("b");
		assertEquals(2, q.size());
		
		q.put("c");
		assertEquals(3, q.size());
		
		q.put("d");
		assertEquals(4, q.size());
		
		q.put("e");
		assertEquals(5, q.size());
		
		assertEquals("a", q.take());
		assertEquals(4, q.size());
		assertEquals("b", q.take());
		assertEquals(3, q.size());
		
		q.put("f");
		q.put("g");
		
		assertEquals(5, q.size());
		assertEquals("c", q.take());
		assertEquals("d", q.take());
		assertEquals("e", q.take());
		assertEquals("f", q.take());
		assertEquals("g", q.take());
		assertEquals(0, q.size());
	}

	@Test
	public void testQueuePutGet6() throws FileNotFoundException, IOException {
		SS ss = new SS();
		//12 header + 512 of data
		MultiFileMMapFifoQueue<String> q = new MultiFileMMapFifoQueue<String>(myTempDir, ss, 12 + 512);
		
		assertEquals(1, myTempDir.listFiles().length);
		for (int i = 0; i < 512; i++) {
			q.put("a");
		}
		
		assertEquals(513, myTempDir.listFiles().length);
		q.shutdownAndDeleteAll();
		assertEquals(0, myTempDir.listFiles().length);
	}
	
	@Test
	public void testQueuePutGet7() throws FileNotFoundException, IOException {
		SS ss = new SS();
		//12 header + 512 of data
		MultiFileMMapFifoQueue<String> q = new MultiFileMMapFifoQueue<String>(myTempDir, ss, 12 + 512);
		
		assertEquals(1, myTempDir.listFiles().length);
		for (int i = 0; i < 512; i++) {
			q.put(i+"");
		}
		
		for (int i = 0; i < 512; i++) {
			assertEquals(i+"", q.take());
		}
	}
	
	@Test
	public void testQueuePutGet8() throws FileNotFoundException, IOException {
		SS ss = new SS();
		//12 header + 512 of data
		MultiFileMMapFifoQueue<String> q = new MultiFileMMapFifoQueue<String>(myTempDir, ss, 12 + 512);
		
		q.put("0");
		for (int i = 1; i <= 512; i++) {
			q.put(i+"");
			assertEquals((i-1)+"", q.take());
		}
		
		assertEquals(512+"", q.take());
		assertEquals(0, q.size());
	}
	
	@Test
	public void testQueuePutGet9() throws FileNotFoundException, IOException {
		SS ss = new SS();
		//12 header + 512 of data
		MultiFileMMapFifoQueue<String> q = new MultiFileMMapFifoQueue<String>(myTempDir, ss, 20);
		assertEquals(1, myTempDir.listFiles().length);
		q.put("0");
		assertEquals(2, myTempDir.listFiles().length);		
		for (int i = 1; i <= 512; i++) {
			String s = i+"";
			q.put(s);
			assertEquals((i-1)+"", q.take());
			assertEquals(i + 2, myTempDir.listFiles().length);
		}
		
		assertEquals(512+"", q.take());
		assertEquals(0, q.size());
	}
	
	public class SS implements Serializer<String> {
		
		public SS() {
		}
		
		@Override
		public byte[] checkpointData(String t) {
			return t.getBytes();
		}

		@Override
		public String interpret(byte[] checkpoint) {
			return new String(checkpoint);
		}
	}
}