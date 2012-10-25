package br.ufmg.dcc.vod.ncrawler.queue.actor;

import br.ufmg.dcc.vod.ncrawler.queue.QueueHandle;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;

public abstract class AbstractActor<T> implements Actor<T> {

	protected final QueueService service;
	protected final int numThreads;
	private boolean started;

	public AbstractActor(int numThreads, QueueService service) {
		this.numThreads = numThreads;
		this.service = service;
		this.started = false;
	}
	
	@Override
	public synchronized final void start() {
		if (this.started) {
			return;
		}
		
		this.started = true;
		for (int i = 0; i < numThreads; i++) {
			service.startProcessor(getQueueHandle(), this);
		}
	}
	
	@Override
	public final void dispatch(T message) {
		try {
			service.sendObjectToQueue(getQueueHandle(), message);
		} catch (InterruptedException e) {
		}
	}

	public abstract QueueHandle getQueueHandle();	
}
