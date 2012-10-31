package br.ufmg.dcc.vod.spiderpig.queue.fd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import br.ufmg.dcc.vod.spiderpig.distributed.nio.service.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.PingPong;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.queue.serializer.MessageLiteSerializer;

import com.google.common.base.Stopwatch;

/**
 * A really basic failure detector. I just considers services as dead if a
 * time out has passed.
 */
public class FailureDetector extends Actor<PingPong> 
		implements QueueProcessor<PingPong>, Runnable {

	private class FDStruct {
		
		final Stopwatch stopwatch;
		final ArrayList<FDListener> listeners;
		boolean down;

		public FDStruct(Stopwatch stopwatch) {
			this.stopwatch = stopwatch;
			this.listeners = new ArrayList<>();
			this.down = false;
		}
		
	}

	public static final String HANDLE = "FDClient";
	private static final long DELTA_MILLI = 500;
	
	private final ReentrantLock lock;
	private final HashMap<ServiceID, FDStruct> monitoring;
	private final long timeout;
	private final TimeUnit unit;
	private final RemoteMessageSender sender;
	private final AtomicBoolean shutdown;
	
	private PingPong ping;
	private Thread thread;

	public FailureDetector(long timeout, TimeUnit unit, 
			RemoteMessageSender sender) {
		super(HANDLE);
		this.timeout = timeout;
		this.unit = unit;
		this.sender = sender;
		this.lock = new ReentrantLock();
		this.monitoring = new HashMap<>();
		this.shutdown = new AtomicBoolean(false);
		this.ping = null;
		this.thread = null;
	}
	
	private PingPong getMsg() {
		if (ping == null)
			ping = PingPong.newBuilder().setCallBackID(getServiceID()).build();
		return ping;
	}
	
	public void addListener(ServiceID serviceID, FDListener listener) {
		try {
			this.lock.lock();
			FDStruct struct = this.monitoring.get(serviceID);
			if (struct == null) {
				Stopwatch stopwatch = new Stopwatch();
				struct = new FDStruct(stopwatch);
				this.monitoring.put(serviceID, struct);
				stopwatch.start();
			}
			struct.listeners.add(listener);
			this.sender.send(serviceID, getMsg());
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public void run() {
		if (this.shutdown.get())
			return;
		
		try {
			this.lock.lock();
			for (ServiceID sid : this.monitoring.keySet()) {
				FDStruct struct = this.monitoring.get(sid);
				long elapsedMillis = struct.stopwatch.elapsedMillis();
				if (!struct.down && elapsedMillis > unit.toMillis(timeout)) {
					struct.stopwatch.stop();
					struct.down = true;
					for (FDListener listener : struct.listeners)
						listener.isSuspected(sid);
				}
				this.sender.send(sid, getMsg());
			}
		} finally {
			this.lock.unlock();
		}
		
		try {
			TimeUnit.MILLISECONDS.sleep(DELTA_MILLI);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public QueueProcessor<PingPong> getQueueProcessor() {
		return this;
	}

	@Override
	public MessageLiteSerializer<PingPong> newMsgSerializer() {
		return new MessageLiteSerializer<>(PingPong.newBuilder());
	}

	@Override
	public void process(PingPong t) {
		try {
			this.lock.lock();
			FDStruct struct = this.monitoring.get(t.getCallBackID());
			
			if (struct != null) { 
				if (struct.down) {
					struct.down = false;
					struct.stopwatch.reset();
					for (FDListener listener : struct.listeners) {
						listener.isUp(t.getCallBackID());
					}
				}
				this.sender.send(t.getCallBackID(), getMsg());
			}
		} finally {
			this.lock.unlock();
		}
	}

	public void startTimer() {
		try {
			this.lock.lock();
			
			if (this.thread != null)
				return;
			
			this.thread = new Thread(this);
			this.thread.run();
		} finally {
			this.lock.unlock();
		}
	}
	
	public void stopTimer() {
		this.shutdown.set(true);
	}	
}