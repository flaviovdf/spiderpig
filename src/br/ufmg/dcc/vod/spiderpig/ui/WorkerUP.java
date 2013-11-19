package br.ufmg.dcc.vod.spiderpig.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.common.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.FDServerActor;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.KillerActor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.jobs.TimeBasedJobExecutor;
import br.ufmg.dcc.vod.spiderpig.worker.Worker;
import br.ufmg.dcc.vod.spiderpig.worker.WorkerActor;

public class WorkerUP implements Command {
	
	public static final String HOSTNAME = "service.hostname";
	public static final String PORT = "service.port";
	private Worker worker;

	@Override
	public void configurate(Configuration configuration,
			ConfigurableBuilder configurableBuilder) throws BuildException {
		
		String hostname = configuration.getString(HOSTNAME);
		int port = configuration.getInt(PORT);
		
		TimeBasedJobExecutor jobExecutor = 
				configurableBuilder.build(TimeBasedJobExecutor.class, 
						configuration);
		
		try {
			QueueService service = new QueueService(hostname, port);
			RemoteMessageSender sender = new RemoteMessageSender();
			
			WorkerActor workerActor = new WorkerActor(jobExecutor, sender);
			workerActor.withSimpleQueue(service);
			
			KillerActor killerActor = new KillerActor(sender);
			killerActor.withSimpleQueue(service);
			
			FDServerActor fdServerActor = new FDServerActor(sender);
			fdServerActor.withSimpleQueue(service);
			
			this.worker = new Worker(workerActor, killerActor, fdServerActor);
		} catch (IOException e) {
			throw new BuildException("io error!", e);
		}
	}

	@Override
	public Set<String> getRequiredParameters() {
		return new HashSet<>(Arrays.asList(HOSTNAME, PORT));
	}

	@Override
	public void exec() throws Exception {
		this.worker.start();
	}
}
