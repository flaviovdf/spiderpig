package br.ufmg.dcc.vod.spiderpig.ui;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import br.ufmg.dcc.vod.spiderpig.common.ServiceIDUtils;
import br.ufmg.dcc.vod.spiderpig.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.CallBackKillActor;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.KillerActor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.PingPong;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.queue.QueueService;

public class WorkerKill extends Command {
	
	private static final String WORKER_HOSTNAME = "w";
	private static final String WORKER_PORT = "t";
	
	private static final String HOSTNAME = "h";
	private static final String PORT = "p";
	
	@SuppressWarnings("static-access")
	@Override
	public Options getOptions() {
		Options opts = new Options();
		
		opts.addOption(OptionBuilder
				.withArgName("worker-hostname")
				.hasArg()
				.isRequired()
				.withDescription("Worker host to kill")
				.create(WORKER_HOSTNAME));

		opts.addOption(OptionBuilder
				.withArgName("worker-port")
				.hasArg()
				.isRequired()
				.withDescription("Worker port to kill")
				.create(WORKER_PORT));
		
		opts.addOption(OptionBuilder
				.withArgName("hostname")
				.hasArg()
				.isRequired()
				.withDescription("A resolvable hostname for callbacks")
				.create(HOSTNAME));
		
		opts.addOption(OptionBuilder
				.withArgName("port")
				.hasArg()
				.isRequired()
				.withDescription("Port to bind socket")
				.create(PORT));
		
		return opts;
	}

	@Override
	public void exec(CommandLine cli) throws Exception {
		String hostname = cli.getOptionValue(HOSTNAME);
		int port = Integer.parseInt(cli.getOptionValue(PORT));
		
		String workerHostname = cli.getOptionValue(WORKER_HOSTNAME);
		int workerPort = Integer.parseInt(cli.getOptionValue(WORKER_PORT));
		
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
	}

}
