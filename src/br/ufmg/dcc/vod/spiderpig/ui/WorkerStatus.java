package br.ufmg.dcc.vod.spiderpig.ui;

import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.ServiceIDUtils;
import br.ufmg.dcc.vod.spiderpig.common.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.FDClientActor;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.FDListener;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.FDServerActor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

public class WorkerStatus extends Command {

	public static final String HOSTNAME = "control.hostname";
	public static final String PORT = "control.port";

	public static final String WORKER_HOSTNAME = "service.hostname";
	public static final String WORKER_PORT = "service.port";
	
	/*
	 * Timeout to declare the service as offline 
	 */
	private static final int TIMEOUT = 5;
	private static final int PING = 1;
	
	@Override
	public void exec(Configuration configuration) throws Exception {
		String hostname = configuration.getString(HOSTNAME);
		int port = configuration.getInt(PORT);
		
		String workerHostname = configuration.getString(WORKER_HOSTNAME);
		int workerPort = configuration.getInt(WORKER_PORT);
		
		RemoteMessageSender sender = new RemoteMessageSender();
		QueueService service = new QueueService(hostname, port);
		
		FDListener listener = new FDStatusListener();
		FDClientActor actor = new FDClientActor(TIMEOUT, PING, 
				TimeUnit.SECONDS, listener, sender);
		ServiceID workerID = ServiceIDUtils.toResolvedServiceID(workerHostname, 
				workerPort, FDServerActor.HANDLE);

		actor.withSimpleQueue(service).startProcessors(1);
		actor.watch(workerID);
		actor.startTimer();
	}
	
	private class FDStatusListener implements FDListener {

		@Override
		public void isUp(ServiceID serviceID) {
			System.out.println("Worker UP");
			System.exit(EXIT_CODES.OK);
		}
		
		@Override
		public void isSuspected(ServiceID serviceID) {
			System.out.println("Worker Down");
			System.exit(EXIT_CODES.OK);
		}
	}
}
