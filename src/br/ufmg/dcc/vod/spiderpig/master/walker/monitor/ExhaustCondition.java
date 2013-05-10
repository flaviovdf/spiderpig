package br.ufmg.dcc.vod.spiderpig.master.walker.monitor;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * The {@link ExhaustCondition} stops the crawl when the queue is empty. It
 * does this by counting the amount of objects dispatched and results received,
 * when they are equal nothing more is on the queue.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class ExhaustCondition extends AbstractStopCondition {

	private enum CounterType {
		DISPATCHED(0),
		OK(1),
		ERROR(2),
		DONE_CONDITION(3);
		
		private final int pos;

		private CounterType(int pos) {
			this.pos = pos;
		}
	}
	
	private final AtomicIntegerArray counters = new AtomicIntegerArray(4);
	
	@Override
	public void dispatched() {
		counters.incrementAndGet(CounterType.DISPATCHED.pos);
		counters.incrementAndGet(CounterType.DONE_CONDITION.pos);
	}

	@Override
	public void resultReceived() {
		counters.incrementAndGet(CounterType.OK.pos);
		decrementDoneCondition();
	}

	@Override
	public void errorReceived() {
		counters.incrementAndGet(CounterType.ERROR.pos);
		decrementDoneCondition();	
	}

	private void decrementDoneCondition() {
		if (counters.decrementAndGet(CounterType.DONE_CONDITION.pos) == 0)
			super.notifyAllListeners();
	}
}
