package br.ufmg.dcc.vod.spiderpig.ui;

import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.ServiceIDUtils;
import br.ufmg.dcc.vod.spiderpig.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.FDClientActor;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.FDListener;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.FDServerActor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.queue.QueueService;

public class WorkerStatus extends Command {

	public static final String HOSTNAME = "control.hostname";
	public static final String PORT = "control.port";

	public static final String WORKER_HOSTNAME = "service.hostname";
	public static final String WORKER_PORT = "service.port";
	
	@Override
	public void exec(Configuration configuration) throws Exception {
		String hostname = configuration.getString(HOSTNAME);
		int port = configuration.getInt(PORT);
		
		String workerHostname = configuration.getString(WORKER_HOSTNAME);
		int workerPort = configuration.getInt(WORKER_PORT);
		
		RemoteMessageSender sender = new RemoteMessageSender();
		QueueService service = new QueueService(hostname, port);
		
		FDListener listener = new FDStatusListener(service);
		FDClientActor actor = new FDClientActor(5, 1, TimeUnit.SECONDS, 
				listener, sender);
		ServiceID workerID = ServiceIDUtils.toResolvedServiceID(workerHostname, 
				workerPort, FDServerActor.HANDLE);

		actor.withSimpleQueue(service).startProcessors(1);
		actor.watch(workerID);
		actor.startTimer();
	}
	
	private class FDStatusListener implements FDListener {

		private final QueueService service;

		public FDStatusListener(QueueService service) {
			this.service = service;
		}
		
		@Override
		public void isUp(ServiceID serviceID) {
			System.out.println("Worker UP");
			new Thread(new ShutdownRunnable()).start();
		}
		
		@Override
		public void isSuspected(ServiceID serviceID) {
			System.out.println("Worker Down");
			new Thread(new ShutdownRunnable()).start();
		}

		private class ShutdownRunnable implements Runnable {

			@Override
			public void run() {
				FDStatusListener.this.service.waitUntilWorkIsDoneAndStop(1);
				System.exit(EXIT_CODES.OK);
			}
			
		}
	}
}
