package br.ufmg.dcc.vod.ncrawler.distributed.commune.client.synccontrol;

import java.util.Set;

import br.edu.ufcg.lsd.commune.container.control.ModuleControlClient;
import br.edu.ufcg.lsd.commune.container.control.ServerModuleController;
import br.edu.ufcg.lsd.commune.identification.ServiceID;
import br.ufmg.dcc.vod.ncrawler.distributed.common.client.DistributedProcessor;

public class EvaluatorUILayer extends ServerModuleController implements EvaluatorManager {

	private final DistributedProcessor p;

	public EvaluatorUILayer(DistributedProcessor p) {
		this.p = p;
	}
	
	@Override
	public void setWorkers(Set<ServiceID> workers) {
		this.p.setWorkers(workers);
	}

	@Override
	public void stop(boolean callExit, boolean force, ModuleControlClient client) {
		//FIXME: stop queues!!!
		super.stop(callExit, force, client);
	}
	
}
