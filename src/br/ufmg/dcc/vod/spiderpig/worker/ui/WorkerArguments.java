package br.ufmg.dcc.vod.spiderpig.worker.ui;

import br.ufmg.dcc.vod.spiderpig.common.config.Arguments;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.FDServerActor;
import br.ufmg.dcc.vod.spiderpig.distributed.fd.KillerActor;
import br.ufmg.dcc.vod.spiderpig.worker.WorkerActor;

public class WorkerArguments implements Arguments {

	private final WorkerActor workerActor;
	private final KillerActor killerActor;
	private final FDServerActor fdServerActor;

	public WorkerArguments(WorkerActor workerActor, KillerActor killerActor,
			FDServerActor fdServerActor) {
		this.workerActor = workerActor;
		this.killerActor = killerActor;
		this.fdServerActor = fdServerActor;
	}

	public WorkerActor getWorkerActor() {
		return workerActor;
	}
	
	public KillerActor getKillerActor() {
		return killerActor;
	}
	
	public FDServerActor getFdServerActor() {
		return fdServerActor;
	}	
}