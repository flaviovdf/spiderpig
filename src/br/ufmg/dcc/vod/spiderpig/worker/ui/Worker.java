package br.ufmg.dcc.vod.spiderpig.worker.ui;

import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.FDServerActor;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.KillerActor;
import br.ufmg.dcc.vod.spiderpig.worker.WorkerActor;

public class Worker {

	private final WorkerActor workerActor;
	private final KillerActor killerActor;
	private final FDServerActor fdServerActor;

	public Worker(WorkerActor workerActor, KillerActor killerActor,
			FDServerActor fdServerActor) {
				this.workerActor = workerActor;
				this.killerActor = killerActor;
				this.fdServerActor = fdServerActor;
	}

	public void start() {
		workerActor.startProcessors(1);
		killerActor.startProcessors(1);
		fdServerActor.startProcessors(1);
	}
	
}
