package br.ufmg.dcc.vod.spiderpig.common.queue;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;

import br.ufmg.dcc.vod.spiderpig.common.queue.serializer.MessageLiteSerializer;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import junit.framework.Assert;
import junit.framework.TestCase;

public class QueueServiceTest extends TestCase {

	private File f1;
	private File f2;
	private File f3;
	private QueueService qs;

	private static CrawlID build(int i) {
		CrawlID.Builder builder = CrawlID.newBuilder();
		return builder.setId(""+i).build();
	}
	
	private class TestActor extends Actor<CrawlID> {

		private final QueueProcessor<CrawlID> processor;

		public TestActor(String handle,
				QueueProcessor<CrawlID> processor) {
			super(handle);
			this.processor = processor;
		}
		
		@Override
		public QueueProcessor<CrawlID> getQueueProcessor() {
			return this.processor;
		}

		@Override
		public MessageLiteSerializer<CrawlID> newMsgSerializer() {
			return new MessageLiteSerializer<>(CrawlID.newBuilder(), null);
		}
	}
	
	@Before
	public void setUp() throws IOException {
		String tmpDir = System.getProperty("java.io.tmpdir");
		do  {
			f1 = new File(tmpDir + File.separator + new Random().nextInt());
		} while (f1.exists());

		do  {
			f3 = new File(tmpDir + File.separator + new Random().nextInt());
		} while (f1.exists());
		
		do  {
			f2 = new File(tmpDir + File.separator + new Random().nextInt());
		} while (f1.exists());
		
		
		f1.mkdirs();
		f2.mkdirs();
		f3.mkdirs();
		qs = new QueueService();
	}

	@After
	public void tearDown() {
		for (File f : f1.listFiles()) {
			f.delete();
			f.deleteOnExit();
		}
		f1.delete();
		f1.deleteOnExit();
		
		for (File f : f2.listFiles()) {
			f.delete();
			f.deleteOnExit();
		}
		f2.delete();
		f2.deleteOnExit();

		
		for (File f : f3.listFiles()) {
			f.delete();
			f.deleteOnExit();
		}
		f3.delete();
		f3.deleteOnExit();

	}
	
	public void testIDs() throws IOException {
		QueueService s1 = new QueueService();
		QueueService s2 = new QueueService();
		
		Assert.assertTrue(s1.getSessionID() != s2.getSessionID());
	}
	
	/*
	 * Multiple threads consuming a queue, waits until the queue is 
	 * empty
	 */
	public void testWaitUntilWorkIsDone1Queue() throws Exception {
		String handle = "1";
		CountDownLatch c = new CountDownLatch(10000);
		
		TestActor actor = 
				new TestActor(handle, new SimpleConsumer(c));
		actor.withSimpleQueue(qs);
		
		baseTestOne1Queue(handle, c, actor);
	}

	private void baseTestOne1Queue(String handle, CountDownLatch c,
			TestActor actor) throws InterruptedException {
		MonitoredSyncQueue q = qs.getMessageQueue(handle);
		
		for (int i = 0 ; i < 10000; i++) {
			actor.dispatch(build(i));
		}
		assertEquals(10000, q.size());
		assertEquals(10000, q.synchronizationData().first.intValue());
		assertEquals(10000, q.synchronizationData().second.intValue());

		actor.startProcessors(10);
		
		qs.waitUntilWorkIsDoneAndStop(1);
		assertTrue(c.await(0, TimeUnit.NANOSECONDS));
		
		assertEquals(0, q.size());
		assertEquals(0, q.synchronizationData().first.intValue());
		assertEquals(10000, q.synchronizationData().second.intValue());
	}
	
	
	/*
	 * Multiple threads consuming multiple queues. Waits until all are
	 * empty 
	 */
	public void testWaitUntilWorkIsDoneMultipleQueues() throws Exception {
		String handle1 = "1";
		String handle2 = "2";
		String handle3 = "3";

		CountDownLatch c = new CountDownLatch(30000);
		TestActor actor1 = 
				new TestActor(handle1, new SimpleConsumer(c));
		TestActor actor2 = 
				new TestActor(handle2, new SimpleConsumer(c));
		TestActor actor3 = 
				new TestActor(handle3, new SimpleConsumer(c));
		
		actor1.withSimpleQueue(qs);
		actor2.withSimpleQueue(qs);
		actor3.withSimpleQueue(qs);
		
		baseTestMultiQueue(handle1, handle2, handle3, c, actor1, actor2, actor3);
	}

	private void baseTestMultiQueue(String handle1, String handle2,
			String handle3, CountDownLatch c, TestActor actor1,
			TestActor actor2, TestActor actor3) throws InterruptedException {
		
		MonitoredSyncQueue q1 = qs.getMessageQueue(handle1);
		MonitoredSyncQueue q2 = qs.getMessageQueue(handle2);
		MonitoredSyncQueue q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 30000; i++) {
			qs.sendObjectToQueue(handle1, build(i));
			qs.sendObjectToQueue(handle2, build(i + 30000));
			qs.sendObjectToQueue(handle3, build(i + 2 * 30000));
		}
		
		assertEquals(30000, q1.size());
		assertEquals(30000, q2.size());
		assertEquals(30000, q3.size());
		assertEquals(30000, q1.synchronizationData().first.intValue());
		assertEquals(30000, q1.synchronizationData().second.intValue());
		assertEquals(30000, q2.synchronizationData().first.intValue());
		assertEquals(30000, q2.synchronizationData().second.intValue());		
		assertEquals(30000, q3.synchronizationData().first.intValue());
		assertEquals(30000, q3.synchronizationData().second.intValue());
		
		for (int i = 0 ; i < 9; i++) {
			if (i % 3 == 0) {
				actor1.startProcessors(1);
			} else if (i % 3 == 1) {
				actor2.startProcessors(1);
			} else if (i % 3 == 2) {
				actor3.startProcessors(1);
			}
		}
		
		qs.waitUntilWorkIsDoneAndStop(1);
		assertTrue(c.await(0, TimeUnit.NANOSECONDS));

		assertEquals(0, q1.size());
		assertEquals(0, q2.size());
		assertEquals(0, q3.size());
		assertEquals(0, q1.synchronizationData().first.intValue());
		assertEquals(30000, q1.synchronizationData().second.intValue());
		assertEquals(0, q2.synchronizationData().first.intValue());
		assertEquals(30000, q2.synchronizationData().second.intValue());		
		assertEquals(0, q3.synchronizationData().first.intValue());
		assertEquals(30000, q3.synchronizationData().second.intValue());
	}
	
	/*
	 * Multiple threads consuming multiple queues. Elements from one queue
	 * are sent to the next. The last queue bounces ONE only element back to
	 * the first 
	 */
	public void testWaitUntilWorkIsDoneExchangedQueueObjects() throws Exception {
		String handle1 = "1";
		String handle2 = "2";
		String handle3 = "3";

		TestActor actor1 = 
				new TestActor(handle1, new NextUpConsumer(qs, handle2));
		TestActor actor2 = 
				new TestActor(handle2, new NextUpConsumer(qs, handle3));
		TestActor actor3 = 
				new TestActor(handle3, new NextUpConsumer(qs, handle1, 1));
		
		actor1.withSimpleQueue(qs);
		actor2.withSimpleQueue(qs);
		actor3.withSimpleQueue(qs);
		
		baseTesteBounce1(handle1, handle2, handle3, actor1, actor2, actor3);
	}

	private void baseTesteBounce1(String handle1, String handle2,
			String handle3, TestActor actor1, TestActor actor2, TestActor actor3)
			throws InterruptedException {
		MonitoredSyncQueue q1 = qs.getMessageQueue(handle1);
		MonitoredSyncQueue q2 = qs.getMessageQueue(handle2);
		MonitoredSyncQueue q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 10000; i++) {
			qs.sendObjectToQueue(handle1, build(i));
		}
		assertEquals(10000, q1.size());
		assertEquals(0, q2.size());
		assertEquals(0, q3.size());

		assertEquals(10000, q1.synchronizationData().first.intValue());
		assertEquals(10000, q1.synchronizationData().second.intValue());
		assertEquals(0, q2.synchronizationData().first.intValue());
		assertEquals(0, q2.synchronizationData().second.intValue());		
		assertEquals(0, q3.synchronizationData().first.intValue());
		assertEquals(0, q3.synchronizationData().second.intValue());

		actor1.startProcessors(1);
		actor2.startProcessors(1);
		actor3.startProcessors(1);
		
		qs.waitUntilWorkIsDoneAndStop(1);
		assertEquals(0, q1.size());
		assertEquals(0, q2.size());
		assertEquals(0, q3.size());
		
		assertEquals(0, q1.synchronizationData().first.intValue());
		assertEquals(10001, q1.synchronizationData().second.intValue());
		assertEquals(0, q2.synchronizationData().first.intValue());
		assertEquals(10001, q2.synchronizationData().second.intValue());		
		assertEquals(0, q3.synchronizationData().first.intValue());
		assertEquals(10001, q3.synchronizationData().second.intValue());
	}
	
	/*
	 * Multiple threads consuming multiple queues. Elements from one queue
	 * are sent to the next. The last queue bounces 100 elements back to
	 * the first 
	 */
	public void testWaitUntilWorkIsDoneExchangedQueueObjects100() throws Exception {
		String handle1 = "1";
		String handle2 = "2";
		String handle3 = "3";

		TestActor actor1 = 
				new TestActor(handle1, new NextUpConsumer(qs, handle2));
		TestActor actor2 = 
				new TestActor(handle2, new NextUpConsumer(qs, handle3));
		TestActor actor3 = 
				new TestActor(handle3, new NextUpConsumer(qs, handle1, 100));
		
		actor1.withSimpleQueue(qs);
		actor2.withSimpleQueue(qs);
		actor3.withSimpleQueue(qs);
		
		baseTesteBounce100(handle1, handle2, handle3, actor1, actor2, actor3);
	}

	private void baseTesteBounce100(String handle1, String handle2,
			String handle3, TestActor actor1, TestActor actor2, TestActor actor3)
			throws InterruptedException {
		MonitoredSyncQueue q1 = qs.getMessageQueue(handle1);
		MonitoredSyncQueue q2 = qs.getMessageQueue(handle2);
		MonitoredSyncQueue q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 10000; i++) {
			qs.sendObjectToQueue(handle1, build(i));
		}
		assertEquals(10000, q1.size());
		assertEquals(0, q2.size());
		assertEquals(0, q3.size());

		assertEquals(10000, q1.synchronizationData().first.intValue());
		assertEquals(10000, q1.synchronizationData().second.intValue());
		assertEquals(0, q2.synchronizationData().first.intValue());
		assertEquals(0, q2.synchronizationData().second.intValue());		
		assertEquals(0, q3.synchronizationData().first.intValue());
		assertEquals(0, q3.synchronizationData().second.intValue());
		
		actor1.startProcessors(1);
		actor2.startProcessors(1);
		actor3.startProcessors(1);
		
		qs.waitUntilWorkIsDoneAndStop(1);

		assertEquals(0, q1.size());
		assertEquals(0, q2.size());
		assertEquals(0, q3.size());
		
		assertEquals(0, q1.synchronizationData().first.intValue());
		assertEquals(10100, q1.synchronizationData().second.intValue());
		assertEquals(0, q2.synchronizationData().first.intValue());
		assertEquals(10100, q2.synchronizationData().second.intValue());		
		assertEquals(0, q3.synchronizationData().first.intValue());
		assertEquals(10100, q3.synchronizationData().second.intValue());
	}
	
	/*
	 * Multiple threads consuming multiple queues. Elements from one queue
	 * are sent to the next. The last queue bounces each element back to
	 * the first twice.
	 */
	public void testWaitUntilWorkIsDoneExchangedQueueObjects2TimesBounce() throws Exception {
		String handle1 = "1";
		String handle2 = "2";
		String handle3 = "3";

		TestActor actor1 = 
				new TestActor(handle1, new NextUpConsumer(qs, handle2));
		TestActor actor2 = 
				new TestActor(handle2, new NextUpConsumer(qs, handle3));
		TestActor actor3 = 
				new TestActor(handle3, new NextUpConsumer(qs, handle1, 20000));
		
		actor1.withSimpleQueue(qs);
		actor2.withSimpleQueue(qs);
		actor3.withSimpleQueue(qs);
		
		baseTestBounce2000(handle1, handle2, handle3, actor1, actor2, actor3);
	}

	private void baseTestBounce2000(String handle1, String handle2,
			String handle3, TestActor actor1, TestActor actor2, TestActor actor3)
			throws InterruptedException {
		MonitoredSyncQueue q1 = qs.getMessageQueue(handle1);
		MonitoredSyncQueue q2 = qs.getMessageQueue(handle2);
		MonitoredSyncQueue q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 10000; i++) {
			qs.sendObjectToQueue(handle1, build(i));
		}
		assertEquals(10000, q1.size());
		assertEquals(0, q2.size());
		assertEquals(0, q3.size());

		assertEquals(10000, q1.synchronizationData().first.intValue());
		assertEquals(10000, q1.synchronizationData().second.intValue());
		assertEquals(0, q2.synchronizationData().first.intValue());
		assertEquals(0, q2.synchronizationData().second.intValue());		
		assertEquals(0, q3.synchronizationData().first.intValue());
		assertEquals(0, q3.synchronizationData().second.intValue());
		
		actor1.startProcessors(1);
		actor2.startProcessors(1);
		actor3.startProcessors(1);
		
		qs.waitUntilWorkIsDoneAndStop(1);

		assertEquals(0, q1.size());
		assertEquals(0, q2.size());
		assertEquals(0, q3.size());
		
		assertEquals(0, q1.synchronizationData().first.intValue());
		assertEquals(30000, q1.synchronizationData().second.intValue());
		assertEquals(0, q2.synchronizationData().first.intValue());
		assertEquals(30000, q2.synchronizationData().second.intValue());		
		assertEquals(0, q3.synchronizationData().first.intValue());
		assertEquals(30000, q3.synchronizationData().second.intValue());
	}
	
	//Now on disk!!
	
	public void testWaitUntilWorkIsDone1QueueD() throws Exception {
		String handle = "1";
		CountDownLatch c = new CountDownLatch(10000);
		
		TestActor actor = 
				new TestActor(handle, new SimpleConsumer(c));
		actor.withFileQueue(qs, f1);
		
		baseTestOne1Queue(handle, c, actor);
	}
	
	
	public void testWaitUntilWorkIsDoneMultipleQueuesD() throws Exception {
		String handle1 = "1";
		String handle2 = "2";
		String handle3 = "3";

		CountDownLatch c = new CountDownLatch(30000);
		TestActor actor1 = 
				new TestActor(handle1, new SimpleConsumer(c));
		TestActor actor2 = 
				new TestActor(handle2, new SimpleConsumer(c));
		TestActor actor3 = 
				new TestActor(handle3, new SimpleConsumer(c));
		
		actor1.withFileQueue(qs, f1);
		actor2.withFileQueue(qs, f2);
		actor3.withFileQueue(qs, f3);
		
		baseTestMultiQueue(handle1, handle2, handle3, c, actor1, actor2, actor3);
	}
	
	public void testWaitUntilWorkIsDoneExchangedQueueObjectsD() throws Exception {
		String handle1 = "1";
		String handle2 = "2";
		String handle3 = "3";

		TestActor actor1 = 
				new TestActor(handle1, new NextUpConsumer(qs, handle2));
		TestActor actor2 = 
				new TestActor(handle2, new NextUpConsumer(qs, handle3));
		TestActor actor3 = 
				new TestActor(handle3, new NextUpConsumer(qs, handle1, 1));
		
		actor1.withFileQueue(qs, f1);
		actor2.withFileQueue(qs, f2);
		actor3.withFileQueue(qs, f3);
		
		baseTesteBounce1(handle1, handle2, handle3, actor1, actor2, actor3);
	}
	
	public void testWaitUntilWorkIsDoneExchangedQueueObjects100D() throws Exception {
		String handle1 = "1";
		String handle2 = "2";
		String handle3 = "3";

		TestActor actor1 = 
				new TestActor(handle1, new NextUpConsumer(qs, handle2));
		TestActor actor2 = 
				new TestActor(handle2, new NextUpConsumer(qs, handle3));
		TestActor actor3 = 
				new TestActor(handle3, new NextUpConsumer(qs, handle1, 100));
		
		actor1.withFileQueue(qs, f1);
		actor2.withFileQueue(qs, f2);
		actor3.withFileQueue(qs, f3);
		
		baseTesteBounce100(handle1, handle2, handle3, actor1, actor2, actor3);
	}
	
	public void testWaitUntilWorkIsDoneExchangedQueueObjects2TimesBounceD() throws Exception {
		String handle1 = "1";
		String handle2 = "2";
		String handle3 = "3";

		TestActor actor1 = 
				new TestActor(handle1, new NextUpConsumer(qs, handle2));
		TestActor actor2 = 
				new TestActor(handle2, new NextUpConsumer(qs, handle3));
		TestActor actor3 = 
				new TestActor(handle3, new NextUpConsumer(qs, handle1, 20000));
		
		actor1.withFileQueue(qs, f1);
		actor2.withFileQueue(qs, f2);
		actor3.withFileQueue(qs, f3);
		
		baseTestBounce2000(handle1, handle2, handle3, actor1, actor2, actor3);
	}
	
	//With a normal and disk queues
	
	public void testWaitUntilWorkIsDoneExchangedQueueObjects2TimesBounceND() throws Exception {
		String handle1 = "1";
		String handle2 = "2";
		String handle3 = "3";

		TestActor actor1 = 
				new TestActor(handle1, new NextUpConsumer(qs, handle2));
		TestActor actor2 = 
				new TestActor(handle2, new NextUpConsumer(qs, handle3));
		TestActor actor3 = 
				new TestActor(handle3, new NextUpConsumer(qs, handle1, 20000));
		
		actor1.withFileQueue(qs, f1);
		actor2.withFileQueue(qs, f2);
		actor3.withSimpleQueue(qs);
		
		baseTestBounce2000(handle1, handle2, handle3, actor1, actor2, actor3);
	}
	
	
	public void testWaitUntilWorkIsDoneExchangedQueueObjects2TimesBounceNDL() throws Exception {
		String handle1 = "1";
		String handle2 = "2";
		String handle3 = "3";

		TestActor actor1 = 
				new TestActor(handle1, new NextUpConsumer(qs, handle2));
		TestActor actor2 = 
				new TestActor(handle2, new NextUpConsumer(qs, handle3));
		TestActor actor3 = 
				new TestActor(handle3, new NextUpConsumer(qs, handle1, 20000));
		
		actor1.withSimpleQueue(qs);
		actor2.withFileQueue(qs, f2);
		actor3.withSimpleQueue(qs);
		
		baseTestBounce2000(handle1, handle2, handle3, actor1, actor2, actor3);
	}
	
	private static class NextUpConsumer implements QueueProcessor<CrawlID> {

		private final String next;
		private final boolean last;
		private final QueueService service;
		
		private int bouncersSent;

		public NextUpConsumer(QueueService service, String next, int bounce) {
			this.service = service;
			this.next = next;
			this.last = true;
			this.bouncersSent = bounce;
		}
		
		public NextUpConsumer(QueueService service, String next) {
			this.service = service;
			this.next = next;
			this.last = false;
			this.bouncersSent = -1;
		}
		
		@Override
		public void process(CrawlID t) {
			if (last) {
				if (bouncersSent != 0) {
					bouncersSent--;
					try {
						service.sendObjectToQueue(next, t);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					service.sendObjectToQueue(next, t);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}			
		}
	}
	
	private static class SimpleConsumer implements QueueProcessor<CrawlID> {

		private final CountDownLatch countDownLatch;

		public SimpleConsumer(CountDownLatch countDownLatch) {
			this.countDownLatch = countDownLatch;
		}
		
		@Override
		public void process(CrawlID t) {
			countDownLatch.countDown();
		}
	}
}