package br.ufmg.dcc.vod.ncrawler.stats;

import java.util.HashMap;
import java.util.Map;

public class StatUpdateMessage {

	public enum Stat {CRAWL_DONE, CRAWL_ERROR, WORKER_UP, WORKER_WORKING, 
		WORKER_SUSPECTED}
	
	private final Map<Stat, Integer> increments;

	public StatUpdateMessage(Map<Stat, Integer> increments) {
		this.increments = increments;
	}
	
	public StatUpdateMessage(int crawlDone, int crawlError, int workerUp,
			int workerWorking, int workerSuspected) {
		this.increments = new HashMap<>();
		this.increments.put(Stat.CRAWL_DONE, crawlDone);
		this.increments.put(Stat.CRAWL_ERROR, crawlError);
		this.increments.put(Stat.WORKER_UP, workerUp);
		this.increments.put(Stat.WORKER_WORKING, workerWorking);
		this.increments.put(Stat.WORKER_SUSPECTED, workerSuspected);
	}
	
	public int getIncrement(Stat stat) {
		return this.increments.get(stat);
	}

	public Map<Stat, Integer> getMap() {
		return increments;
	}
}