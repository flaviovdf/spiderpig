package br.ufmg.dcc.vod.spiderpig.ui;

import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.ServiceIDUtils;
import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.common.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.CallBackKillActor;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.KillerActor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.PingPong;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

import com.google.common.collect.Sets;

public class WorkerKill implements Command {

	public static final String HOSTNAME = "control.hostname";
	public static final String PORT = "control.port";

	public static final String WORKER_HOSTNAME = "service.hostname";
	public static final String WORKER_PORT = "service.port";
	
	private String hostname;
	private int port;
	
	private String workerHostname;
	private int workerPort;
	
	@Override
	public void configurate(Configuration configuration,
			ConfigurableBuilder builder) throws BuildException {
		this.hostname = configuration.getString(HOSTNAME);
		this.port = configuration.getInt(PORT);
		this.workerHostname = configuration.getString(WORKER_HOSTNAME);
		this.workerPort = configuration.getInt(WORKER_PORT);
	}

	@Override
	public Set<String> getRequiredParameters() {
		return Sets.newHashSet(HOSTNAME, PORT, WORKER_HOSTNAME, WORKER_PORT);
	}

	@Override
	public void exec() throws Exception {
		QueueService service = new QueueService(hostname, port);
		CallBackKillActor actor = new CallBackKillActor();
		actor.withSimpleQueue(service).startProcessors(1);
		
		ServiceID callbackID = 
				ServiceIDUtils.toResolvedServiceID(hostname, port, 
						CallBackKillActor.HANDLE);
		RemoteMessageSender sender = new RemoteMessageSender();
		sender.send(ServiceIDUtils.toResolvedServiceID(workerHostname, 
				workerPort, 
				KillerActor.HANDLE), 
				PingPong.newBuilder()
				.setSessionID(1l)
				.setCallBackID(callbackID).build());
		System.exit(EXIT_CODES.OK);
	}
}