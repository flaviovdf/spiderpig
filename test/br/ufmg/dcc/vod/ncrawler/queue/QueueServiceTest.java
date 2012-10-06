package br.ufmg.dcc.vod.ncrawler.queue;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

public class QueueServiceTest extends TestCase {

	private File f1;
	private File f2;
	private File f3;
	private QueueService qs = new QueueService();
	
	@Before
	public void setUp() {
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
	
	/*
	 * Multiple threads consuming a queue, waits until the queue is 
	 * empty
	 */
	public void testWaitUntilWorkIsDone1Queue() throws Exception {
		QueueHandle handle = qs.createMessageQueue();
		MonitoredSyncQueue<?> q = qs.getMessageQueue(handle);
		
		for (int i = 0 ; i < 10000; i++) {
			qs.sendObjectToQueue(handle, i);
		}
		assertEquals(10000, q.size());
		assertEquals(10000, q.synchronizationData().first.intValue());
		assertEquals(10000, q.synchronizationData().second.intValue());
		
		CountDownLatch c = new CountDownLatch(10000);
		for (int i = 0 ; i < 10; i++) {
			qs.startProcessor(handle, new SimpleConsumer(c));
		}
		
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
		QueueHandle handle1 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q1 = qs.getMessageQueue(handle1);
		QueueHandle handle2 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q2 = qs.getMessageQueue(handle2);
		QueueHandle handle3 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 30000; i++) {
			qs.sendObjectToQueue(handle1, i);
			qs.sendObjectToQueue(handle2, i + 30000);
			qs.sendObjectToQueue(handle3, i + 2 * 30000);
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
		
		CountDownLatch c = new CountDownLatch(30000);
		for (int i = 0 ; i < 9; i++) {
			if (i % 3 == 0) {
				qs.startProcessor(handle1, new SimpleConsumer(c));
			} else if (i % 3 == 1) {
				qs.startProcessor(handle2, new SimpleConsumer(c));
			} else if (i % 3 == 2) {
				qs.startProcessor(handle3, new SimpleConsumer(c));
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
		QueueHandle handle1 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q1 = qs.getMessageQueue(handle1);
		QueueHandle handle2 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q2 = qs.getMessageQueue(handle2);
		QueueHandle handle3 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 10000; i++) {
			qs.sendObjectToQueue(handle1, i);
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
		
		qs.startProcessor(handle1, new NextUpConsumer(qs, handle2));
		qs.startProcessor(handle2, new NextUpConsumer(qs, handle3));
		qs.startProcessor(handle3, new NextUpConsumer(qs, handle1, 1));
		
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
		QueueHandle handle1 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q1 = qs.getMessageQueue(handle1);
		QueueHandle handle2 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q2 = qs.getMessageQueue(handle2);
		QueueHandle handle3 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 10000; i++) {
			qs.sendObjectToQueue(handle1, i);
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
		
		qs.startProcessor(handle1, new NextUpConsumer(qs, handle2));
		qs.startProcessor(handle2, new NextUpConsumer(qs, handle3));
		qs.startProcessor(handle3, new NextUpConsumer(qs, handle1, 100));
		
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
		QueueHandle handle1 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q1 = qs.getMessageQueue(handle1);
		QueueHandle handle2 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q2 = qs.getMessageQueue(handle2);
		QueueHandle handle3 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 10000; i++) {
			qs.sendObjectToQueue(handle1, i);
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
		
		qs.startProcessor(handle1, new NextUpConsumer(qs, handle2));
		qs.startProcessor(handle2, new NextUpConsumer(qs, handle3));
		qs.startProcessor(handle3, new NextUpConsumer(qs, handle1, 20000));
		
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
		SI si = new SI();
		
		QueueHandle handle = qs.createPersistentMessageQueue(f1, si, 1024 * 1024);
		MonitoredSyncQueue<?> q = qs.getMessageQueue(handle);
		
		for (int i = 0 ; i < 10000; i++) {
			qs.sendObjectToQueue(handle, i);
		}
		assertEquals(10000, q.size());
		assertEquals(10000, q.synchronizationData().first.intValue());
		assertEquals(10000, q.synchronizationData().second.intValue());
		
		CountDownLatch c = new CountDownLatch(10000);
		for (int i = 0 ; i < 10; i++) {
			qs.startProcessor(handle, new SimpleConsumer(c));
		}
		
		qs.waitUntilWorkIsDoneAndStop(1);
		assertTrue(c.await(0, TimeUnit.NANOSECONDS));
		
		assertEquals(0, q.size());
		assertEquals(0, q.synchronizationData().first.intValue());
		assertEquals(10000, q.synchronizationData().second.intValue());
	}
	
	
	public void testWaitUntilWorkIsDoneMultipleQueuesD() throws Exception {
		SI si = new SI();
		
		QueueHandle handle1 = qs.createPersistentMessageQueue(f1, si, 1024 * 1024);
		MonitoredSyncQueue<?> q1 = qs.getMessageQueue(handle1);
		QueueHandle handle2 = qs.createPersistentMessageQueue(f2, si, 1024 * 1024);
		MonitoredSyncQueue<?> q2 = qs.getMessageQueue(handle2);
		QueueHandle handle3 = qs.createPersistentMessageQueue(f3, si, 1024 * 1024);
		MonitoredSyncQueue<?> q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 30000; i++) {
			qs.sendObjectToQueue(handle1, i);
			qs.sendObjectToQueue(handle2, i + 30000);
			qs.sendObjectToQueue(handle3, i + 2 * 30000);
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
		
		CountDownLatch c = new CountDownLatch(30000);
		for (int i = 0 ; i < 9; i++) {
			if (i % 3 == 0) {
				qs.startProcessor(handle1, new SimpleConsumer(c));
			} else if (i % 3 == 1) {
				qs.startProcessor(handle2, new SimpleConsumer(c));
			} else if (i % 3 == 2) {
				qs.startProcessor(handle3, new SimpleConsumer(c));
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
	
	public void testWaitUntilWorkIsDoneExchangedQueueObjectsD() throws Exception {
		SI si = new SI();
		
		QueueHandle handle1 = qs.createPersistentMessageQueue(f1, si, 1024 * 1024);
		MonitoredSyncQueue<?> q1 = qs.getMessageQueue(handle1);
		QueueHandle handle2 = qs.createPersistentMessageQueue(f2, si, 1024 * 1024);
		MonitoredSyncQueue<?> q2 = qs.getMessageQueue(handle2);
		QueueHandle handle3 = qs.createPersistentMessageQueue(f3, si, 1024 * 1024);
		MonitoredSyncQueue<?> q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 10000; i++) {
			qs.sendObjectToQueue(handle1, i);
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
		
		qs.startProcessor(handle1, new NextUpConsumer(qs, handle2));
		qs.startProcessor(handle2, new NextUpConsumer(qs, handle3));
		qs.startProcessor(handle3, new NextUpConsumer(qs, handle1, 1));
		
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
	
	public void testWaitUntilWorkIsDoneExchangedQueueObjects100D() throws Exception {
		SI si = new SI();
		
		QueueHandle handle1 = qs.createPersistentMessageQueue(f1, si, 1024 * 1024);
		MonitoredSyncQueue<?> q1 = qs.getMessageQueue(handle1);
		QueueHandle handle2 = qs.createPersistentMessageQueue(f2, si, 1024 * 1024);
		MonitoredSyncQueue<?> q2 = qs.getMessageQueue(handle2);
		QueueHandle handle3 = qs.createPersistentMessageQueue(f3, si, 1024 * 1024);
		MonitoredSyncQueue<?> q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 10000; i++) {
			qs.sendObjectToQueue(handle1, i);
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
		
		qs.startProcessor(handle1, new NextUpConsumer(qs, handle2));
		qs.startProcessor(handle2, new NextUpConsumer(qs, handle3));
		qs.startProcessor(handle3, new NextUpConsumer(qs, handle1, 100));
		
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
	
	public void testWaitUntilWorkIsDoneExchangedQueueObjects2TimesBounceD() throws Exception {
		SI si = new SI();
		
		QueueHandle handle1 = qs.createPersistentMessageQueue(f1, si, 1024 * 1024);
		MonitoredSyncQueue<?> q1 = qs.getMessageQueue(handle1);
		QueueHandle handle2 = qs.createPersistentMessageQueue(f2, si, 1024 * 1024);
		MonitoredSyncQueue<?> q2 = qs.getMessageQueue(handle2);
		QueueHandle handle3 = qs.createPersistentMessageQueue(f3, si, 1024 * 1024);
		MonitoredSyncQueue<?> q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 10000; i++) {
			qs.sendObjectToQueue(handle1, i);
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
		
		qs.startProcessor(handle1, new NextUpConsumer(qs, handle2));
		qs.startProcessor(handle2, new NextUpConsumer(qs, handle3));
		qs.startProcessor(handle3, new NextUpConsumer(qs, handle1, 20000));
		
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
	
	//With a normal and disk queues
	
	public void testWaitUntilWorkIsDoneExchangedQueueObjects2TimesBounceND() throws Exception {
		SI si = new SI();
		
		QueueHandle handle1 = qs.createPersistentMessageQueue(f1, si, 1024 * 1024);
		MonitoredSyncQueue<?> q1 = qs.getMessageQueue(handle1);
		QueueHandle handle2 = qs.createPersistentMessageQueue(f2, si, 1024 * 1024);
		MonitoredSyncQueue<?> q2 = qs.getMessageQueue(handle2);
		QueueHandle handle3 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 10000; i++) {
			qs.sendObjectToQueue(handle1, i);
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
		
		qs.startProcessor(handle1, new NextUpConsumer(qs, handle2));
		qs.startProcessor(handle2, new NextUpConsumer(qs, handle3));
		qs.startProcessor(handle3, new NextUpConsumer(qs, handle1, 20000));
		
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
	
	
	//OneHas LIMITS!!
	
	public void testWaitUntilWorkIsDoneExchangedQueueObjects2TimesBounceNDL() throws Exception {
		SI si = new SI();
		
		QueueHandle handle1 = qs.createPersistentMessageQueue(f1, si, 1024 * 1024);
		MonitoredSyncQueue<?> q1 = qs.getMessageQueue(handle1);
		QueueHandle handle2 = qs.createLimitedBlockMessageQueue(2);
		MonitoredSyncQueue<?> q2 = qs.getMessageQueue(handle2);
		QueueHandle handle3 = qs.createMessageQueue();
		MonitoredSyncQueue<?> q3 = qs.getMessageQueue(handle3);
		
		for (int i = 0 ; i < 10000; i++) {
			qs.sendObjectToQueue(handle1, i);
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
		
		qs.startProcessor(handle1, new NextUpConsumer(qs, handle2));
		qs.startProcessor(handle2, new NextUpConsumer(qs, handle3));
		qs.startProcessor(handle3, new NextUpConsumer(qs, handle1, 20000));
		
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
	
	private static class NextUpConsumer implements QueueProcessor<Integer> {

		private final QueueHandle next;
		private final boolean last;
		private final QueueService service;
		
		private int bouncersSent;

		public NextUpConsumer(QueueService service, QueueHandle next, int bounce) {
			this.service = service;
			this.next = next;
			this.last = true;
			this.bouncersSent = bounce;
		}
		
		public NextUpConsumer(QueueService service, QueueHandle next) {
			this.service = service;
			this.next = next;
			this.last = false;
			this.bouncersSent = -1;
		}
		
		@Override
		public String getName() {
			return "NextUp!";
		}

		@Override
		public void process(Integer t) {
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
	
	private static class SimpleConsumer implements QueueProcessor<Integer> {

		private final CountDownLatch countDownLatch;

		public SimpleConsumer(CountDownLatch countDownLatch) {
			this.countDownLatch = countDownLatch;
		}
		
		@Override
		public String getName() {
			return "Simple";
		}

		@Override
		public void process(Integer t) {
			countDownLatch.countDown();
		}
	}
	
	private class SI implements Serializer<Integer> {
		@Override
		public byte[] checkpointData(Integer t) {
			int i = t.intValue();
			byte[] b = new byte[4];
			
			ByteBuffer wrap = ByteBuffer.wrap(b);
			wrap.putInt(i);
			
			return b;
		}

		@Override
		public Integer interpret(byte[] checkpoint) {
			return ByteBuffer.wrap(checkpoint).getInt();
		}
	}
}