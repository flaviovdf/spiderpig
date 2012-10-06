package br.ufmg.dcc.vod.ncrawler.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.junit.Test;


public class MemoryMappedQueueTest extends TestCase {

	@Test
	public void testQueuePutErrors() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
		
		try {
			q.put("a");
			fail();
		} catch (QueueServiceException e) {
		}

		q.createAndOpen();
		q.put("a");
		
		q.shutdownAndSync();
		
		try {
			assertEquals("a", q.take());
		} catch (QueueServiceException e) {
		} 

		try {
			q.shutdownAndSync();
		} catch (QueueServiceException e) {
		} 
	
		try {
			q.shutdownAndDelete();
		} catch (QueueServiceException e) {
		} 

		f.delete();
	}
	
	@Test
	public void testQueuePutErrors2() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
		q.createAndOpen();
		
		try {
			q.take();
			fail();
		} catch (QueueServiceException e) {
		}
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGet() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
		q.createAndOpen();
		
		assertEquals(0, q.size());
		q.put("a");
		assertEquals(1, q.size());
		assertEquals("a", q.take());
		assertEquals(0, q.size());
		
		f.delete();
	}


	@Test
	public void testQueuePutGetLimit() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		try {
			new MemoryMappedFIFOQueue<String>(f, ss, 12);
			fail();
		} catch (QueueServiceException e) {
		}
		
		f.delete();
	}

	@Test
	public void testQueuePutGetLimit2() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 15);
		q.createAndOpen();

		q.put("a"); //This will succeed!
		
		try {
			q.put("a");
		} catch (QueueServiceException e) {
		}
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGetLimit3() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 18);
		q.createAndOpen();

		q.put("a"); //This will succeed!
		q.put("a");
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGetLimit4() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 18);
		q.createAndOpen();
		
		assertEquals(6, q.remaining());
		q.put("a");
		assertEquals(3, q.remaining());
		q.put("a");
		assertEquals(0, q.remaining());
		
		try {
			q.put("a");
		} catch (QueueServiceException e) {
		}
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGetLimit5() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 16);
		q.createAndOpen();
		
		assertEquals(4, q.remaining());
		q.put("aa"); //16 - 12header - 2data -1len
		assertEquals(0, q.remaining());
		
		try {
			q.put("a");
		} catch (QueueServiceException e) {
		}
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGet2() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
		q.createAndOpen();
		
		assertEquals(12, q.getStart());
		assertEquals(12, q.getEnd());
		assertEquals(0, q.size());
		q.put("a");
		assertEquals(12, q.getStart());
		assertEquals(15, q.getEnd());
		assertEquals(1, q.size());
		
		q.put("b");
		assertEquals(12, q.getStart());
		assertEquals(18, q.getEnd());
		assertEquals(2, q.size());
		
		q.put("c");
		assertEquals(12, q.getStart());
		assertEquals(21, q.getEnd());
		assertEquals(3, q.size());
		
		q.put("d");
		assertEquals(12, q.getStart());
		assertEquals(24, q.getEnd());
		assertEquals(4, q.size());
		
		q.put("e");
		assertEquals(12, q.getStart());
		assertEquals(27, q.getEnd());
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
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGet3() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
		q.createAndOpen();
		
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
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGet4() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
		q.createAndOpen();
		
		assertEquals(12, q.getStart());
		assertEquals(12, q.getEnd());
		assertEquals(0, q.size());
		q.put("a");
		assertEquals(12, q.getStart());
		assertEquals(15, q.getEnd()); 
		assertEquals(1, q.size());
		
		q.put("b");
		assertEquals(12, q.getStart());
		assertEquals(18, q.getEnd());
		assertEquals(2, q.size());
		
		q.put("c");
		assertEquals(12, q.getStart());
		assertEquals(21, q.getEnd());
		assertEquals(3, q.size());
		
		q.put("d");
		assertEquals(12, q.getStart());
		assertEquals(24, q.getEnd());
		assertEquals(4, q.size());
		
		q.put("e");
		assertEquals(12, q.getStart());
		assertEquals(27, q.getEnd());
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
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGet5() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
		q.createAndOpen();
		
		assertEquals(12, q.getStart());
		assertEquals(12, q.getEnd());
		assertEquals(0, q.size());
		q.put("a");
		assertEquals(12, q.getStart());
		assertEquals(15, q.getEnd()); //17 because the size is also written. 1 bytes containing the size of the information and another containing the informatation
		assertEquals(1, q.size());
		
		q.put("b");
		assertEquals(12, q.getStart());
		assertEquals(18, q.getEnd());
		assertEquals(2, q.size());
		
		q.put("c");
		assertEquals(12, q.getStart());
		assertEquals(21, q.getEnd());
		assertEquals(3, q.size());
		
		q.put("d");
		assertEquals(12, q.getStart());
		assertEquals(24, q.getEnd());
		assertEquals(4, q.size());
		
		q.put("e");
		assertEquals(12, q.getStart());
		assertEquals(27, q.getEnd());
		assertEquals(5, q.size());
		
		assertEquals("a", q.take());
		assertEquals(4, q.size());
		assertEquals("b", q.take());
		assertEquals(3, q.size());

		q.shutdownAndSync();
		q.reopen();
		assertEquals(3, q.size());
		assertEquals(18, q.getStart());
		assertEquals(27, q.getEnd());
		
		q.put("f");
		assertEquals(18, q.getStart());
		assertEquals(30, q.getEnd());
		
		q.put("g");
		assertEquals(18, q.getStart());
		assertEquals(33, q.getEnd());
		
		assertEquals(5, q.size());
		assertEquals("c", q.take());
		assertEquals("d", q.take());
		assertEquals("e", q.take());
		assertEquals("f", q.take());
		assertEquals("g", q.take());
		assertEquals(0, q.size());
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGet6() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
		q.createAndOpen();
		
		q.put("a");
		q.put("b");
		assertEquals("a", q.take());
		assertEquals(1, q.size());

		q.shutdownAndDelete();
		
		try {
			q.reopen();
			fail();
		} catch (IOException e) {}
		
		q.createAndOpen();
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGetFileStructure1() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
		q.createAndOpen();
		
		q.put("avcl");
		q.shutdownAndSync();
		
		FileInputStream fs = new FileInputStream(f);
		byte[] b = new byte[1024 * 1024];
		fs.read(b);
		fs.close();
		
		ByteBuffer wrap = ByteBuffer.wrap(b);
		assertEquals(1, wrap.getInt());
		assertEquals(1, q.size());
		
		assertEquals(12, wrap.getInt()); //starts at 12 bytes
		assertEquals(12, q.getStart()); 
		
		assertEquals("avcl".getBytes().length + 12 + 2, wrap.getInt()); 
		assertEquals("avcl".getBytes().length + 12 + 2, q.getEnd());
		
		assertEquals(4, wrap.getShort()); //size of info
		
		byte[] r = new byte[4];
		wrap.get(r);
		assertEquals("avcl", new String(r));
		
		while (wrap.limit() != wrap.position())
			assertEquals(0, wrap.get());
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGetFileStructure2() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
		q.createAndOpen();
		
		q.put("a");
		
		q.take();
		q.shutdownAndSync();
		
		FileInputStream fs = new FileInputStream(f);
		byte[] b = new byte[1024 * 1024];
		fs.read(b);
		fs.close();
		
		ByteBuffer wrap = ByteBuffer.wrap(b);
		assertEquals(0, wrap.getInt());
		assertEquals(0, q.size());
		
		int e = "a".getBytes().length + 12 + 2;
		assertEquals(e, wrap.getInt()); //starts at 15 bytes, element deleted but remains on file (2bytes)
		assertEquals(e, q.getStart()); 
		
		assertEquals(e, wrap.getInt()); 
		assertEquals(e, q.getEnd());
		
		//Garbage data
		assertEquals(1, wrap.getShort());
		assertEquals("a", new String(new byte[]{wrap.get()}));
		
		while (wrap.limit() != wrap.position())
			assertEquals(0, wrap.get());
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGetFileStructure3() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
		q.createAndOpen();
		
		q.put("a");
		q.put("v");
		q.put("c");
		q.put("l");
		
		q.take();
		q.take();
		q.shutdownAndSync();
		
		
		FileInputStream fs = new FileInputStream(f);
		byte[] b = new byte[1024 * 1024];
		fs.read(b);
		fs.close();
		
		ByteBuffer wrap = ByteBuffer.wrap(b);
		assertEquals(2, wrap.getInt());
		assertEquals(2, q.size());
		
		assertEquals(18, wrap.getInt());
		assertEquals(18, q.getStart()); 
		
		assertEquals(24, wrap.getInt()); 
		assertEquals(24, q.getEnd());
		
		//Garbage
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("a", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("v", new String(new byte[]{wrap.get()}));
		
		//Real Data
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("c", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("l", new String(new byte[]{wrap.get()}));
		
		while (wrap.limit() != wrap.position())
			assertEquals(0, wrap.get());
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGetFileStructure4() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
		q.createAndOpen();
		
		q.put("a");
		q.put("v");
		q.put("c");
		q.put("l");
		
		q.take();
		q.take();
		q.put("z");
		q.shutdownAndSync();
		
		
		FileInputStream fs = new FileInputStream(f);
		byte[] b = new byte[1024 * 1024];
		fs.read(b);
		fs.close();
		
		ByteBuffer wrap = ByteBuffer.wrap(b);
		assertEquals(3, wrap.getInt());
		assertEquals(3, q.size());
		
		assertEquals(18, wrap.getInt()); //starts at 16 bytes
		assertEquals(18, q.getStart()); 
		
		assertEquals(27, wrap.getInt()); 
		assertEquals(27, q.getEnd());
		
		//Garbage
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("a", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("v", new String(new byte[]{wrap.get()}));
		
		//Real Data
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("c", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("l", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("z", new String(new byte[]{wrap.get()}));
		
		while (wrap.limit() != wrap.position())
			assertEquals(0, wrap.get());
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGetFileStructure5() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
		q.createAndOpen();
		
		q.put("a");
		q.put("v");
		q.put("c");
		q.put("l");
		
		q.take();
		q.take();
		q.put("z");
		q.put("e");
		q.put("b");
		q.shutdownAndSync();
		
		
		FileInputStream fs = new FileInputStream(f);
		byte[] b = new byte[1024 * 1024];
		fs.read(b);
		fs.close();
		
		ByteBuffer wrap = ByteBuffer.wrap(b);
		assertEquals(5, wrap.getInt());
		assertEquals(5, q.size());
		
		assertEquals(18, wrap.getInt()); //starts at 16 bytes
		assertEquals(18, q.getStart()); 
		
		assertEquals(33, wrap.getInt()); 
		assertEquals(33, q.getEnd());
		
		//Garbage
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("a", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("v", new String(new byte[]{wrap.get()}));
		
		//Real Data
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("c", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("l", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("z", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("e", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.getShort()); //size of info
		assertEquals("b", new String(new byte[]{wrap.get()}));
		
		while (wrap.limit() != wrap.position())
			assertEquals(0, wrap.get());
		
		f.delete();
	}
	
//	public void testAllCompact() throws Exception {
//		File f = File.createTempFile("temp", "test");
//		f.deleteOnExit();
//		
//		SS ss = new SS();
//		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
//		q.createAndOpen();
//		
//		assertEquals(0, q.size());
//		q.put("a");
//		assertEquals(1, q.size());
//		
//		q.put("b");
//		assertEquals(2, q.size());
//		
//		q.put("c");
//		assertEquals(3, q.size());
//		
//		q.put("d");
//		assertEquals(4, q.size());
//		
//		q.put("e");
//		assertEquals(5, q.size());
//		
//		assertEquals("a", q.take());
//		assertEquals(4, q.size());
//		assertEquals("b", q.take());
//		assertEquals(3, q.size());
//		assertEquals("c", q.take());
//		assertEquals(2, q.size());
//		assertEquals("d", q.take());
//		assertEquals(1, q.size());
//		assertEquals("e", q.take());
//		assertEquals(0, q.size());
//		
//		f.delete();
//	}
//	
//	public void testLimitNoCompact() throws Exception {
//		File f = File.createTempFile("temp", "test");
//		f.deleteOnExit();
//		
//		SS ss = new SS();
//		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
//		q.createAndOpen();
//
//		assertEquals(12, q.getStart());
//		assertEquals(12, q.getEnd());
//		
//		String s = "";
//		for (int i = 0; i < 256; i++) {
//			s += "a";
//		}
//		
//		try {
//			q.put(s);
//			fail();
//		} catch (Exception e) {
//			
//		}
//		
//		f.delete();
//	}
//	
//	public void testLimitCompact() throws Exception {
//		File f = File.createTempFile("temp", "test");
//		f.deleteOnExit();
//		
//		SS ss = new SS();
//		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
//		q.createAndOpen();
//
//		assertEquals(12, q.getStart());
//		assertEquals(12, q.getEnd());
//		
//		String s = "";
//		for (int i = 0; i < 256; i++) {
//			s += "a";
//		}
//		
//		q.put(s);
//		assertEquals(s, q.take());
//		
//		f.delete();
//	}
//	
//	public void testLimitCompact2() throws Exception {
//		File f = File.createTempFile("temp", "test");
//		f.deleteOnExit();
//		
//		SS ss = new SS();
//		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
//		q.createAndOpen();
//
//		assertEquals(12, q.getStart());
//		assertEquals(12, q.getEnd());
//		
//		String s = "";
//		for (int i = 0; i < 204; i++) { //204 limits exactly to 255bytes
//			s += i;
//		}
//		
//		q.put(s);
//		assertEquals(s, q.take());
//		
//		f.delete();
//	}
//
//	public void testLimitCompact3() throws Exception {
//		File f = File.createTempFile("temp", "test");
//		f.deleteOnExit();
//		
//		SS ss = new SS();
//		MemoryMappedFIFOQueue<String> q = new MemoryMappedFIFOQueue<String>(f, ss, 1024 * 1024);
//		q.createAndOpen();
//
//		assertEquals(12, q.getStart());
//		assertEquals(12, q.getEnd());
//		
//		String s = "";
//		for (int i = 0; i < 205; i++) { //204 limits exactly to 255bytes
//			s += i;
//		}
//		
//		try {
//			q.put(s);
//			fail();
//		} catch (QueueServiceException e) {
//			
//		}
//		
//		f.delete();
//	}
	
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
