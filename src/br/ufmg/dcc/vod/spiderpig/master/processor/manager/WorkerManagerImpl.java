package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

import com.google.common.annotations.VisibleForTesting;

public class WorkerManagerImpl implements WorkerManager {

	public enum WorkerState {IDLE, BUSY, SUSPECTED}

	private Map<WorkerState, Collection<ServiceID>> stateMap;
	private Map<CrawlID, ServiceID> allocMap;
	private Map<ServiceID, CrawlID> inverseAllocMap;
	private ReentrantLock lock;
	private Condition waitCondition;
	
	WorkerManagerImpl(Collection<ServiceID> workerIDs, WorkerState initial) {
		this.stateMap = new HashMap<>();
		
		this.stateMap.put(WorkerState.IDLE, new LinkedList<ServiceID>());
		this.stateMap.put(WorkerState.BUSY, new HashSet<ServiceID>());
		this.stateMap.put(WorkerState.SUSPECTED, new HashSet<ServiceID>());
		
		this.stateMap.get(initial).addAll(workerIDs);
		this.allocMap = new HashMap<>();
		this.inverseAllocMap = new HashMap<>();
		this.lock = new ReentrantLock();
		this.waitCondition = this.lock.newCondition();
	}
	
	public WorkerManagerImpl(Collection<ServiceID> workerIDs) {
		this(workerIDs, WorkerState.SUSPECTED);
	}
	
	@Override
	public ServiceID allocateAvailableExecutor(CrawlID crawlID) 
			throws InterruptedException {
		try {
			this.lock.lock();
		
			if (this.allocMap.containsKey(crawlID))
				return null;
			
			LinkedList<ServiceID> idle = (LinkedList<ServiceID>) 
					this.stateMap.get(WorkerState.IDLE);
			while (idle.size() == 0)
				this.waitCondition.await();
			
			//Can only reach here with at least one element
			ServiceID workerID = idle.remove(0);
			this.stateMap.get(WorkerState.BUSY).add(workerID);
			this.allocMap.put(crawlID, workerID);
			this.inverseAllocMap.put(workerID, crawlID);
			return workerID;
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public boolean freeExecutor(CrawlID crawlID) {
		try {
			this.lock.lock();
			ServiceID workerID = this.allocMap.remove(crawlID);
			if (workerID != null) {
				CrawlID remove = this.inverseAllocMap.remove(workerID);
				assert remove.equals(crawlID);
				
				this.stateMap.get(WorkerState.BUSY).remove(workerID);
				this.stateMap.get(WorkerState.IDLE).add(workerID);
				this.waitCondition.signal();
				return true;
			} else {
				return false;
			}
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public CrawlID executorSuspected(ServiceID workerID) {
		try {
			this.lock.lock();
			CrawlID crawlID = this.inverseAllocMap.remove(workerID);
			if (crawlID != null) {
				ServiceID remove = this.allocMap.remove(crawlID);
				assert remove.equals(workerID);
				this.stateMap.get(WorkerState.BUSY).remove(workerID);
			} else {
				this.stateMap.get(WorkerState.IDLE).remove(workerID);
			}
			
			if (!this.stateMap.get(WorkerState.SUSPECTED).contains(workerID))
				this.stateMap.get(WorkerState.SUSPECTED).add(workerID);
			
			return crawlID;
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public void markAvailable(ServiceID workerID) {
		try {
			this.lock.lock();
			
			if (this.stateMap.get(WorkerState.SUSPECTED).remove(workerID)) {
				assert !this.inverseAllocMap.containsKey(workerID);
			}
			
			if (this.inverseAllocMap.containsKey(workerID)) {
				CrawlID crawlID = this.inverseAllocMap.remove(workerID);
				
				ServiceID removedID = this.allocMap.remove(crawlID);
				assert removedID.equals(workerID);
				boolean removedBusy = 
						this.stateMap.get(WorkerState.BUSY).remove(workerID);
				assert removedBusy;
			} else {
				this.stateMap.get(WorkerState.IDLE).remove(workerID);
			}
			
			assert !this.allocMap.containsKey(workerID);
			assert !this.stateMap.get(WorkerState.IDLE).contains(workerID);
			assert !this.stateMap.get(WorkerState.BUSY).contains(workerID);
			assert !this.stateMap.get(WorkerState.SUSPECTED).contains(workerID);
			
			this.stateMap.get(WorkerState.IDLE).add(workerID);
			this.waitCondition.signal();
		} finally {
			this.lock.unlock();
		}
	}

	@VisibleForTesting Collection<ServiceID> getByState(WorkerState state) {
		return this.stateMap.get(state);
	}
}