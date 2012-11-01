package br.ufmg.dcc.vod.spiderpig.queue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;

import br.ufmg.dcc.vod.spiderpig.common.ServiceIDUtils;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.queue.serializer.MessageLiteSerializer;

import com.google.protobuf.MessageLite;

public abstract class Actor<T extends MessageLite> {

	private final String handle;
	protected QueueService service;
	private ServiceID serviceID;

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
	
	public final ServiceID getServiceID() {
		if (this.serviceID != null)
			return serviceID;
		
		try {
			this.serviceID = ServiceIDUtils.toResolvedServiceID(service.getIP(), 
					service.getPort(), getHandle());
			return serviceID;
		} catch (UnknownHostException e) {
			throw new QueueServiceException(e);
		}
	}
	
	public abstract QueueProcessor<T> getQueueProcessor();

	public abstract MessageLiteSerializer<T> newMsgSerializer();
	
}