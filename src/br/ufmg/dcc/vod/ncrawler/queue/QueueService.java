package br.ufmg.dcc.vod.ncrawler.queue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import br.ufmg.dcc.vod.ncrawler.common.Pair;

/**
 * QueueServices are used to create MonitoredSyncQueues, add objects to these
 * queues and register threads which will consume objects from queues. This
 * class is thread safe.
 * 
 * @param <T> Type of objects in a queue
 */
public class QueueService {

	private final Map<QueueHandle, MonitoredSyncQueue<?>> ids = Collections.synchronizedMap(new HashMap<QueueHandle, MonitoredSyncQueue<?>>());
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final AtomicInteger i = new AtomicInteger(0);

	/**
	 * Creates a new message queue
	 * 
	 * @return A queue handle
	 */
	public QueueHandle createMessageQueue() {
		return createMessageQueue("");
	}


	/**
	 * Creates a new message queue with a label
	 *
	 * @param label Label
	 * @return A queue handle
	 */
	public <T> QueueHandle createMessageQueue(String label) {
		QueueHandle h = new QueueHandle(i.incrementAndGet());
		this.ids.put(h, new MonitoredSyncQueue<T>(label, new SimpleEventQueue<T>()));
		return h;
	}
	
	/**
	 * Creates a new message queue that is stored on multiple files on a disk
	 *
	 * @param label Label
	 * @param f Folder to use
	 * @param serializer To serialize object
	 * @param bytes amount of bytes to allocate on file
	 * 
	 * @return A queue handle
	 * 
	 * @throws IOException In case and io error occurs 
	 * @throws FileNotFoundException  In case the file does not exist
	 */
	@SuppressWarnings("unchecked")
	public QueueHandle createPersistentMessageQueue(File f, Serializer serializer, int bytes) throws FileNotFoundException, IOException {
		return createPersistentMessageQueue("", f, serializer, bytes);
	}


	/**
	 * Creates a new message queue that is stored on multiple files on disk
	 *
	 * @param label Label
	 * @param f Folder to use
	 * @param serializer To serialize object
	 * @param bytes amount of bytes to allocate on file
	 * 
	 * @return A queue handle
	 * 
	 * @throws IOException In case and io error occurs 
	 * @throws FileNotFoundException  In case the file does not exist
	 */
	public <T> QueueHandle createPersistentMessageQueue(String label, File f, Serializer<T> serializer, int bytes) throws FileNotFoundException, IOException {
		QueueHandle h = new QueueHandle(i.incrementAndGet());
		MultiFileMMapFifoQueue<T> memoryMappedQueue = new MultiFileMMapFifoQueue<T>(f, serializer, bytes);
		this.ids.put(h, new MonitoredSyncQueue<T>(label, memoryMappedQueue));
		return h;
	}
	
	/**
	 * Creates a new message queue which can hold a limited number of objects
	 * 
	 * @param max Maximum number of elements 
	 * 
	 * @return A queue handle
	 */
	public QueueHandle createLimitedBlockMessageQueue(int max) {
		return createLimitedBlockMessageQueue("", max);
	}


	/**
	 * Creates a new message queue which can hold a limited number of objects
	 *
	 * @param label Label
	 * @param max Maximum number of elements
	 * 
	 * @return A queue handle
	 */
	public <T> QueueHandle createLimitedBlockMessageQueue(String label, int max) {
		QueueHandle h = new QueueHandle(i.incrementAndGet());
		this.ids.put(h, new MonitoredSyncQueue<T>(label, new SimpleEventQueue<T>(), max));
		return h;
	}
	
	/**
	 * Starts a QueueProcessor on a new Thread. It will consume the queue with
	 * the given handle.
	 * 
	 * @param h Handle identifying the queue
	 * @param p QueueProcessor object which will process
	 */
	public <T> void startProcessor(QueueHandle h, QueueProcessor<T> p) {
		if (!this.ids.containsKey(h)) {
			throw new QueueServiceException("Unknown handle");
		}
	
		WorkerRunnable<T> runnable = new WorkerRunnable<T>((MonitoredSyncQueue<T>) this.ids.get(h), p);
		executor.execute(runnable);
	}

	/**
	 * Insert an object to a given queue so it can be processed by a QueueProcessor.
	 * 
	 * @param h The handle of the queue
	 * @param t The object to insert
	 * 
	 * @throws InterruptedException 
	 */
	public <T> void sendObjectToQueue(QueueHandle h, T t) throws InterruptedException {
		if (!this.ids.containsKey(h)) {
			throw new QueueServiceException("Unknown handle");
		}
	
		((MonitoredSyncQueue<T>) this.ids.get(h)).put(t);
	}
	
	/**
	 * Waits until multiple queues are empty. The algorithm does two passes on the
	 * queues, one to see if they are empty and the other to assure that the work
	 * that was being done by the queues did not add any objects on the other queues. 
	 * <br>
	 * When using queues for communication it is important to only add an element to a
	 * communication queue after calling the <code>done</code> method on the queue the
	 * current thread is consuming. This is done by the <code>WorkerRunnable</code> class. 
	 * 
	 * @param secondsBetweenChecks - Seconds between verifications
	 */
	public void waitUntilWorkIsDone(int secondsBetweenChecks) {
		boolean someoneIsWorking = false;
		do {
			synchronized (ids) {
				someoneIsWorking = false;
				
				//System.err.println("-- debug " + new Date());
				//for (MonitoredSyncQueue<?> m : ids.values()) {
				//	System.err.println(m + " => " + m.size());
				//}
				
				//Acquiring time stamps
				int[] stamps = new int[ids.size()];
				int i = 0;
				for (MonitoredSyncQueue<?> m : ids.values()) {
					Pair<Integer, Integer> sizeAndTimeStamp = m.synchronizationData();
					if (sizeAndTimeStamp.first != 0) {
						someoneIsWorking = true;
						break;
					} else {
						stamps[i] = sizeAndTimeStamp.second;
					}
					i++;
				}
	
				//Verifying if stamps changed
				i = 0;
				if (!someoneIsWorking) {
					for (MonitoredSyncQueue<?> m : ids.values()) {
						Pair<Integer, Integer> sizeAndTimeStamp = m.synchronizationData();
						if (sizeAndTimeStamp.first != 0 || stamps[i] != sizeAndTimeStamp.second) {
							someoneIsWorking = true;
							break;
						}
						i++;
					}
				}
			}
			
			if (someoneIsWorking) {
				try {
					Thread.sleep(secondsBetweenChecks * 1000);
				} catch (InterruptedException e) {
				}
			}
		} while (someoneIsWorking);
	}
	
	public void waitUntilWorkIsDoneAndStop(int secondsBetweenChecks) {
		waitUntilWorkIsDone(secondsBetweenChecks);
		this.executor.shutdownNow();
	}
	
	/**
	 * A worker runnable guarantees that the done method of the queue is called. 
	 */
	private class WorkerRunnable<T> extends Thread {
		
		private final MonitoredSyncQueue<T> q;
		private final QueueProcessor<T> p;
		
		public WorkerRunnable(MonitoredSyncQueue<T> q, QueueProcessor<T> p) {
			super("WorkerRunnable: " + p.getName());
			this.q = q;
			this.p = p;
		}
		
		@Override
		public void run() {
			while (true) {
				boolean interrupted = false;
				T take = null;
				try {
					take = q.claim();
					p.process(take);
				} catch (InterruptedException e) {
					interrupted = true;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (!interrupted) q.done(take);
				}
			}
		}
	}
	
	protected MonitoredSyncQueue<?> getMessageQueue(QueueHandle handle) {
		return this.ids.get(handle);
	}
}
