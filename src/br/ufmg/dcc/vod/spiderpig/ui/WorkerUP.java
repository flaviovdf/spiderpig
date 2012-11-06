package br.ufmg.dcc.vod.spiderpig.ui;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.worker.ui.Worker;
import br.ufmg.dcc.vod.spiderpig.worker.ui.WorkerFactory;

public class WorkerUP extends Command {

	@Override
	public void exec(Configuration configuration) throws Exception {

		Worker worker = 
				new WorkerFactory().configurate(configuration);

		worker.start();
		System.out.println("Worker Started");
	}
}
