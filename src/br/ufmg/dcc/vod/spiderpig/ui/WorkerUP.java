package br.ufmg.dcc.vod.spiderpig.ui;

import java.lang.reflect.Constructor;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import br.ufmg.dcc.vod.spiderpig.common.LoggerInitiator;
import br.ufmg.dcc.vod.spiderpig.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.FDServerActor;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.KillerActor;
import br.ufmg.dcc.vod.spiderpig.distributed.worker.WorkerActor;
import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueService;

public class WorkerUP extends Command {

	private static final String HOSTNAME = "h";
	private static final String PORT = "p";
	private static final String LOG_FILE = "l";
	private static final String EXECUTOR_CLASS = "e";
	private static final String SLEEP_TIME = "t";

	@Override
	@SuppressWarnings("static-access")
	public Options getOptions() {
		Options opts = new Options();
		Option portOpt = OptionBuilder
				.withArgName("port")
				.hasArg()
				.isRequired()
				.withDescription("Port to Bind")
				.create(PORT);
		
		Option logFileOpt = OptionBuilder
				.withArgName("file")
				.hasArg()
				.isRequired()
				.withDescription("Log File")
				.create(LOG_FILE);
		
		Option executorOpt = OptionBuilder
				.withArgName("executor")
				.hasArg()
				.isRequired()
				.withDescription("Executor class to use")
				.create(EXECUTOR_CLASS);
		
		Option sleepTimeOpt = OptionBuilder
				.withArgName("sleep-time")
				.hasArg()
				.isRequired()
				.withDescription("Sleep time (seconds)")
				.create(SLEEP_TIME);
		
		opts.addOption(OptionBuilder
				.withArgName("hostname")
				.hasArg()
				.isRequired()
				.withDescription("A resolvable hostname for callbacks")
				.create(HOSTNAME));
		
		opts.addOption(portOpt);
		opts.addOption(logFileOpt);
		opts.addOption(executorOpt);
		opts.addOption(sleepTimeOpt);
		
		return opts;
	}

	@Override
	public void exec(CommandLine cli) throws Exception {
		
		String hostname = cli.getOptionValue(HOSTNAME);
		long sleepTime = Long.parseLong(cli.getOptionValue(SLEEP_TIME)) * 1000;
		int port = Integer.parseInt(cli.getOptionValue(PORT));
		LoggerInitiator.initiateLog(cli.getOptionValue(LOG_FILE));
		String cls = cli.getOptionValue(EXECUTOR_CLASS);
		
		Constructor<?> constructor = Class.forName(cls)
				.getConstructor(long.class);
		JobExecutor executor = (JobExecutor) constructor.newInstance(sleepTime);
		
		QueueService service = new QueueService(hostname, port);
		RemoteMessageSender sender = new RemoteMessageSender();
		
		WorkerActor workerActor = new WorkerActor(executor, sender);
		workerActor.withSimpleQueue(service).startProcessors(1);
		
		KillerActor killerActor = new KillerActor(sender);
		killerActor.withSimpleQueue(service).startProcessors(1);
		
		FDServerActor fdServerActor = new FDServerActor(sender);
		fdServerActor.withSimpleQueue(service).startProcessors(1);
	}
}
