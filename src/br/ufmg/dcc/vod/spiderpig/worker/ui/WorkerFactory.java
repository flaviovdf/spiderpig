package br.ufmg.dcc.vod.spiderpig.worker.ui;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.common.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.FDServerActor;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.KillerActor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableRequester;
import br.ufmg.dcc.vod.spiderpig.jobs.TimeBasedJobExecutor;
import br.ufmg.dcc.vod.spiderpig.worker.WorkerActor;

public class WorkerFactory extends AbstractConfigurable<Worker> {

	public static final String HOSTNAME = "service.hostname";
	public static final String PORT = "service.port";
	public static final String REQUESTER = "worker.requester";

	@Override
	public Worker realConfigurate(Configuration configuration) 
			throws Exception {
		
		String hostname = configuration.getString(HOSTNAME);
		int port = configuration.getInt(PORT);
		String requesterClass = configuration.getString(REQUESTER);
		
		Constructor<?> constructor = Class.forName(requesterClass)
				.getConstructor();
		ConfigurableRequester requester = 
				(ConfigurableRequester) constructor.newInstance();
		
		requester.configurate(configuration);
		
		TimeBasedJobExecutor jobExecutor = new TimeBasedJobExecutor(requester);
		jobExecutor.configurate(configuration);
		
		QueueService service = new QueueService(hostname, port);
		RemoteMessageSender sender = new RemoteMessageSender();
		
		WorkerActor workerActor = new WorkerActor(jobExecutor, sender);
		workerActor.withSimpleQueue(service);
		
		KillerActor killerActor = new KillerActor(sender);
		killerActor.withSimpleQueue(service);
		
		FDServerActor fdServerActor = new FDServerActor(sender);
		fdServerActor.withSimpleQueue(service);
		
		return new Worker(workerActor, killerActor, fdServerActor);
	}

	@Override
	public Set<String> getRequiredParameters() {
		return new HashSet<>(Arrays.asList(HOSTNAME, PORT, REQUESTER));
	}
}