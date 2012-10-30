package br.ufmg.dcc.vod.spiderpig.queue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import br.ufmg.dcc.vod.spiderpig.queue.serializer.MessageLiteSerializer;

import com.google.protobuf.MessageLite;

public abstract class Actor<T extends MessageLite> {

	private final String handle;
	
	private QueueService service;

	public Actor(String label) {
		this.handle = label;
	}

	@SuppressWarnings("unchecked")
	public final <R extends Actor<T>> R withSimpleQueue(QueueService service) {
		if (this.service != null)
			throw new QueueServiceException("Already attached to a service");
		
		this.service = service;
		this.service.createMessageQueue(this);
		return (R) this;
	}
	
	@SuppressWarnings("unchecked")
	public <R extends Actor<?>> R withFileQueue(QueueService service, 
			File folder, int bytes) 
			throws FileNotFoundException, IOException {
		if (this.service != null)
			throw new QueueServiceException("Already attached to a service");
		
		this.service = service;
		this.service.createPersistentMessageQueue(this, folder, bytes);
		return (R) this;
	}

	public <R extends Actor<?>> R withFileQueue(QueueService service, File folder) 
			throws FileNotFoundException, IOException {
		
		int size = 1024 * 1024 * 5;
		return withFileQueue(service, folder, size);
	}
	
	public final void startProcessors(int numThreads) {
		for (int i = 0; i < numThreads; i++) {
			service.startProcessor(handle, getQueueProcessor());
		}
	}
	
	public final void dispatch(T message) {
		try {
			service.sendObjectToQueue(handle, message);
		} catch (InterruptedException e) {
		}
	}
	
	public final String getHandle() {
		return handle;
	}
	
	public abstract QueueProcessor<T> getQueueProcessor();

	public abstract MessageLiteSerializer<T> newMsgSerializer();
	
}