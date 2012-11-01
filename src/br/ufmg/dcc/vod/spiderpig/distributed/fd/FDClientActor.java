package br.ufmg.dcc.vod.spiderpig.distributed.fd;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import br.ufmg.dcc.vod.spiderpig.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.PingPong;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.queue.serializer.MessageLiteSerializer;

import com.google.common.base.Stopwatch;

/**
 * A really basic failure detector. I just considers services as dead if a
 * time out has passed.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class FDClientActor extends Actor<PingPong> 
		implements QueueProcessor<PingPong>, Runnable {

	private class FDStruct {
		
		final Stopwatch stopwatch;
		boolean down;
		long previd;

		public FDStruct(Stopwatch stopwatch) {
			this.stopwatch = stopwatch;
			this.down = true;
			this.previd = 0;
		}
		
	}

	public static final String HANDLE = "FDClient";
	
	private final ReentrantLock lock;
	private final HashMap<ServiceID, FDStruct> monitoring;
	private final long timeout;
	private final long pingTime;
	private final TimeUnit unit;
	private final RemoteMessageSender sender;
	private final AtomicBoolean shutdown;
	private final FDListener listener;
	
	private PingPong ping;
	private Thread thread;

	public FDClientActor(long timeout, long pingTime, TimeUnit unit, 
			FDListener listener, RemoteMessageSender sender) {
		super(HANDLE);
		this.timeout = timeout;
		this.pingTime = pingTime;
		this.unit = unit;
		this.listener = listener;
		this.sender = sender;
		this.lock = new ReentrantLock();
		this.monitoring = new HashMap<>();
		this.shutdown = new AtomicBoolean(false);
		this.ping = null;
		this.thread = null;
	}
	
	private PingPong getMsg() {
		if (ping == null)
			ping = PingPong.newBuilder()
					.setCallBackID(getServiceID())
					.setSessionID(service.getSessionID())
					.build();
		return ping;
	}
	
	public void watch(ServiceID serviceID) {
		try {
			this.lock.lock();
			FDStruct struct = this.monitoring.get(serviceID);
			if (struct == null) {
				Stopwatch stopwatch = new Stopwatch();
				struct = new FDStruct(stopwatch);
				this.monitoring.put(serviceID, struct);
				stopwatch.start();
			}
			this.sender.send(serviceID, getMsg());
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public void run() {
		while(!this.shutdown.get()) {
			try {
				this.lock.lock();
				for (ServiceID sid : this.monitoring.keySet()) {
					FDStruct struct = this.monitoring.get(sid);
					long elapsedMillis = struct.stopwatch.elapsedMillis();
					if (!struct.down && elapsedMillis > unit.toMillis(timeout)) {
						setDown(sid, struct);
					}
					this.sender.send(sid, getMsg());
				}
			} finally {
				this.lock.unlock();
			}
			
			try {
				unit.sleep(pingTime);
			} catch (InterruptedException e) {
			}
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
					setUp(t, struct);
				//If ids changed then service died and rebourned
				} else if (struct.previd != t.getSessionID()) {
					setDown(t.getCallBackID(), struct);
					setUp(t, struct);
					
				}
			}
		} finally {
			this.lock.unlock();
		}
	}

	private void setUp(PingPong pingPong, FDStruct struct) {
		struct.previd = pingPong.getSessionID();
		struct.down = false;
		struct.stopwatch.reset();
		struct.stopwatch.start();
		listener.isUp(pingPong.getCallBackID());
	}

	private void setDown(ServiceID sid, FDStruct struct) {
		struct.stopwatch.stop();
		struct.down = true;
		listener.isSuspected(sid);
	}
	
	public void startTimer() {
		try {
			this.lock.lock();
			
			if (this.thread != null)
				return;
			
			this.thread = new Thread(this);
			this.thread.start();
		} finally {
			this.lock.unlock();
		}
	}
	
	public void stopTimer() throws InterruptedException {
		this.shutdown.set(true);
		this.thread.join();
	}	
}