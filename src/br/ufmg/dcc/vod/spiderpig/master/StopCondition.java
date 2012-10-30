package br.ufmg.dcc.vod.spiderpig.master;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class StopCondition {

	public enum CounterType {
		DISPATCHED(0),
		OK(1),
		ERROR(2),
		STOP_CONDITION(3);
		
		private final int pos;

		private CounterType(int pos) {
			this.pos = pos;
		}
	}
	
	private final AtomicIntegerArray counters = new AtomicIntegerArray(4);
	private final SynchronousQueue<Object> toAwait = new SynchronousQueue<>();
	
	public void dispatched() {
		counters.incrementAndGet(CounterType.DISPATCHED.pos);
		counters.incrementAndGet(CounterType.STOP_CONDITION.pos);
	}

	public void resultReceived() {
		counters.incrementAndGet(CounterType.OK.pos);
		decrementCondition();
	}

	public void errorReceived() {
		counters.incrementAndGet(CounterType.ERROR.pos);
		decrementCondition();		
	}
	
	private void decrementCondition() {
		if (counters.decrementAndGet(CounterType.STOP_CONDITION.pos) == 0)
			try {
				toAwait.put(new Object());
			} catch (InterruptedException e) {
			}
	}
	
	public void awaitAllDone() {
		try {
			this.toAwait.take();
		} catch (InterruptedException e) {
		}
	}
	
	public int getCounter(CounterType type) {
		return counters.get(type.pos);
	}
}