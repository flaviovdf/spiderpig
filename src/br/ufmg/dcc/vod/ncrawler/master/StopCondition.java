package br.ufmg.dcc.vod.ncrawler.master;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class StopCondition {

	private final AtomicInteger found = new AtomicInteger(0);
	private final SynchronousQueue<Object> toAwait;
	
	public StopCondition() {
		this.toAwait = new SynchronousQueue<>();
	}
	
	public void dispatched() {
		found.incrementAndGet();
	}

	public void resultReceived() {
		if (found.decrementAndGet() == 0)
			try {
				toAwait.put(new Object());
			} catch (InterruptedException e) {
			}
	}

	public void await() {
		try {
			this.toAwait.take();
		} catch (InterruptedException e) {
		}
	}

}
