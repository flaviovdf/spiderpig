package br.ufmg.dcc.vod.ncrawler.queue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import br.ufmg.dcc.vod.ncrawler.common.Tuple;

/**
 * A synchronized queue of objects. This is a thread safe queue in which objects
 * retrieved by a <code>peek</code> operation are counted on a current work
 * atomic integer. In order to remove objects from this buffers, users of this class
 * must manually inform that have processed whatever information was needed from
 * the taken object (this is done using the <code>done</done> method. When an object
 * is removed the atomic integer is decreased, if it reaches 0 it means that no thread
 * has objects acquired.
 * <br>
 * This is done in order to monitor the state of queues in a way that if a thread is
 * processing a given object taken from a queue, we cannot say that the work is done 
 * because that thread may insert work again on this queue or other queues.
 * <br>
 * When using queues for communication it is important to only add an element to a
 * communication queue after calling the <code>done</code> method on the queue
 * the current thread is consuming.
 */
class MonitoredSyncQueue {

	public static final Object POISON = new Object();
	private volatile boolean poisoned = false;
	
	//Stamps
	private final AtomicInteger workHandle = new AtomicInteger(0);
	private final AtomicInteger timeStamp = new AtomicInteger(0);
	private final ReentrantReadWriteLock stampLock;
	
	//Get lock
	private final ReentrantLock lock;
	private final Condition getCondition;
	
	private final String label;
	
	private final EventQueue<Object> e;

	@SuppressWarnings({"rawtypes", "unchecked"})
	public MonitoredSyncQueue(String label, EventQueue e) {
		this.label = label;
		this.e = e;
		this.stampLock = new ReentrantReadWriteLock();
		this.lock = new ReentrantLock();
		this.getCondition = lock.newCondition();
	}

	public void put(Object t) {
		try {
			stampLock.writeLock().lock();
			this.workHandle.incrementAndGet();
			this.timeStamp.incrementAndGet();
		} finally {
			stampLock.writeLock().unlock();
		}
		
		try {
			lock.lock();
			e.put(t);
			getCondition.signal();
		} finally {
			lock.unlock();
		}
	}

	public Object claim() {
		try {
			lock.lock();
			while (e.size() == 0 && !poisoned)
				try {
					getCondition.await();
				} catch (InterruptedException e) {
				}

			if (!poisoned) {
				Object take = e.take();
				return take;
			} else
				return POISON;
		} finally {
			lock.unlock();
		}
	}

	public void done(Object claimed) {
		try {
			stampLock.writeLock().lock();
			this.workHandle.decrementAndGet();
		} finally {
			stampLock.writeLock().unlock();
		}
	}

	public int size() {
		return e.size();
	}

	public Tuple<Integer, Integer> synchronizationData() {
		try {
			stampLock.readLock().lock();
			return new Tuple<Integer, Integer>(workHandle.get(), timeStamp.get());
		} finally {
			stampLock.readLock().unlock();
		}
	}
	
	@Override
	public String toString() {
		return label;
	}

	public void poison() {
		lock.lock();
		poisoned = true;
		getCondition.signalAll();
		lock.unlock();
	}
}