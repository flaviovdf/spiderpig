package br.ufmg.dcc.vod.ncrawler.master.processor.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkerManagerImpl implements WorkerManager {

	public enum WorkerState {IDLE, BUSY, SUSPECTED}

	private Map<WorkerState, List<WorkerID>> stateMap;
	private Map<String, WorkerID> allocMap;
	private Map<WorkerID, String> inverseAllocMap;
	
	public WorkerManagerImpl(Collection<WorkerID> workerIDs) {
		this.stateMap = new HashMap<>();
		for (WorkerState state : WorkerState.values())
			this.stateMap.put(state, new LinkedList<WorkerID>());
		
		this.stateMap.get(WorkerState.IDLE).addAll(workerIDs);
		this.allocMap = new HashMap<>();
		this.inverseAllocMap = new HashMap<>();
	}
	
	@Override
	public synchronized WorkerID allocateAvailableExecutor(String crawlID) 
			throws InterruptedException { 
		
		if (this.allocMap.containsKey(crawlID))
			return null;
		
		if (this.stateMap.get(WorkerState.IDLE).size() == 0)
			wait();
		
		WorkerID returnVal = this.stateMap.get(WorkerState.IDLE).remove(0);
		this.stateMap.get(WorkerState.BUSY).add(returnVal);
		this.allocMap.put(crawlID, returnVal);
		this.inverseAllocMap.put(returnVal, crawlID);
		return returnVal;
	}

	@Override
	public synchronized boolean freeExecutor(String crawlID) {
		WorkerID workerID = this.allocMap.remove(crawlID);
		if (workerID != null) {
			String remove = this.inverseAllocMap.remove(workerID);
			assert remove == crawlID;
			
			this.stateMap.get(WorkerState.BUSY).remove(workerID);
			this.stateMap.get(WorkerState.IDLE).add(workerID);
			notify();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized void executorSuspected(WorkerID workerID) {
		String crawlID = this.inverseAllocMap.remove(workerID);
		
		if (crawlID != null) {
			WorkerID remove = this.allocMap.remove(crawlID);
			assert remove == workerID;
			assert this.stateMap.get(WorkerState.BUSY).remove(workerID);
		} else {
			assert this.stateMap.get(WorkerState.IDLE).remove(workerID);
		}
		
		this.stateMap.get(WorkerState.SUSPECTED).add(workerID);
	}

	@Override
	public synchronized void markAvailable(WorkerID workerID) {
		
		if (this.stateMap.get(WorkerState.SUSPECTED).remove(workerID)) {
			assert !this.inverseAllocMap.containsKey(workerID);
		}
		
		if (this.inverseAllocMap.containsKey(workerID)) {
			String crawlID = this.inverseAllocMap.remove(workerID);
			
			assert this.allocMap.remove(crawlID).equals(workerID);
			assert this.stateMap.get(WorkerState.BUSY).remove(workerID);
		} else {
			this.stateMap.get(WorkerState.IDLE).remove(workerID);
			
		}
		
		assert !this.allocMap.containsKey(workerID);
		assert !this.stateMap.get(WorkerState.IDLE).contains(workerID);
		assert !this.stateMap.get(WorkerState.BUSY).contains(workerID);
		assert !this.stateMap.get(WorkerState.SUSPECTED).contains(workerID);
		
		this.stateMap.get(WorkerState.IDLE).add(workerID);
		notify();
	}
}