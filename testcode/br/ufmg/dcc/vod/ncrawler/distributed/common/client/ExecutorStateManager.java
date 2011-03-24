package br.ufmg.dcc.vod.ncrawler.distributed.common.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import br.edu.ufcg.lsd.commune.identification.ServiceID;
import br.ufmg.dcc.vod.ncrawler.distributed.commune.server.JobExecutor;

public class ExecutorStateManager {

	private enum State {IDLE, BUSY, OFFLINE}
	private Map<State, Collection<ServiceID>> scheduleMap;
	private Map<ServiceID, JobExecutor> lookup;
	
	public ExecutorStateManager(Set<ServiceID> workers) {
		this.scheduleMap = new HashMap<State, Collection<ServiceID>>();
		this.lookup = new HashMap<ServiceID, JobExecutor>();
		
		this.scheduleMap.put(State.IDLE, new LinkedList<ServiceID>());
		this.scheduleMap.put(State.BUSY, new LinkedList<ServiceID>());
		this.scheduleMap.put(State.OFFLINE, new LinkedList<ServiceID>());
		
		for (ServiceID e : workers) {
			this.scheduleMap.get(State.IDLE).add(e);
		}
	}
	
	public synchronized JobExecutor getNextAvailableExecutor() throws InterruptedException {
		if (this.scheduleMap.get(State.IDLE).isEmpty()) {
			wait();
		}
		
		ServiceID eid = ((LinkedList<ServiceID>)this.scheduleMap.get(State.IDLE)).removeFirst();
		this.scheduleMap.get(State.BUSY).add(eid);
		return this.lookup.get(eid);
	}
	
	public synchronized void executorDied(ServiceID eid) {
		this.scheduleMap.get(State.BUSY).remove(eid);
		this.scheduleMap.get(State.IDLE).remove(eid);
		this.scheduleMap.get(State.OFFLINE).add(eid);
		this.lookup.remove(eid);
	}
	
	public synchronized void executorUp(ServiceID eid, JobExecutor e) {
		this.scheduleMap.get(State.IDLE).add(eid);
		this.lookup.put(eid, e);
		this.scheduleMap.get(State.OFFLINE).remove(eid);
		notify();
	}
	
	public synchronized void releaseExecutor(ServiceID eid) {
		this.scheduleMap.get(State.BUSY).remove(eid);
		this.scheduleMap.get(State.IDLE).add(eid);
		notify();
	}
}
