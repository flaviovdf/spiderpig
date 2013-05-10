package br.ufmg.dcc.vod.spiderpig.ui;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.ServiceIDUtils;
import br.ufmg.dcc.vod.spiderpig.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.CallBackKillActor;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.KillerActor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.PingPong;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.queue.QueueService;

public class WorkerKill extends Command {

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
		
		QueueService service = new QueueService(hostname, port);
		CallBackKillActor actor = new CallBackKillActor();
		actor.withSimpleQueue(service).startProcessors(1);
		
		ServiceID callbackID = 
				ServiceIDUtils.toResolvedServiceID(hostname, port, 
						CallBackKillActor.HANDLE);
		RemoteMessageSender sender = new RemoteMessageSender();
		sender.send(ServiceIDUtils.toResolvedServiceID(workerHostname, workerPort, 
				KillerActor.HANDLE), 
				PingPong.newBuilder()
				.setSessionID(1l)
				.setCallBackID(callbackID).build());
		System.exit(EXIT_CODES.OK);
	}

}
