package br.ufmg.dcc.vod.spiderpig.worker.ui;

import java.lang.reflect.Constructor;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.Configurable;
import br.ufmg.dcc.vod.spiderpig.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.FDServerActor;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.KillerActor;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableJobExecutor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.worker.WorkerActor;

public class WorkerFactory implements Configurable<WorkerArguments> {

	public static final String HOSTNAME = "service.hostname";
	public static final String PORT = "service.port";
	public static final String JOB = "worker.job";

	@Override
	public WorkerArguments configurate(Configuration configuration) 
			throws Exception {
		
		String hostname = configuration.getString(HOSTNAME);
		int port = configuration.getInt(PORT);
		String jobExecClass = configuration.getString(JOB);
		
		Constructor<?> constructor = Class.forName(jobExecClass)
				.getConstructor();
		ConfigurableJobExecutor executor = 
				(ConfigurableJobExecutor) constructor.newInstance();
		
		executor.configurate(configuration);
		
		QueueService service = new QueueService(hostname, port);
		RemoteMessageSender sender = new RemoteMessageSender();
		
		WorkerActor workerActor = new WorkerActor(executor, sender);
		workerActor.withSimpleQueue(service);
		
		KillerActor killerActor = new KillerActor(sender);
		killerActor.withSimpleQueue(service);
		
		FDServerActor fdServerActor = new FDServerActor(sender);
		fdServerActor.withSimpleQueue(service);
		
		return new WorkerArguments(workerActor, killerActor, fdServerActor);
	}
}