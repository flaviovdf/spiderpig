package br.ufmg.dcc.vod.spiderpig.ui;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.worker.ui.WorkerArguments;
import br.ufmg.dcc.vod.spiderpig.worker.ui.WorkerFactory;

public class WorkerUP extends Command {

	@Override
	public void exec(Configuration configuration) throws Exception {

		WorkerArguments configurate = 
				new WorkerFactory().configurate(configuration);
		
		configurate.getWorkerActor().startProcessors(1);
		configurate.getFdServerActor().startProcessors(1);
		configurate.getKillerActor().startProcessors(1);
	}
}
