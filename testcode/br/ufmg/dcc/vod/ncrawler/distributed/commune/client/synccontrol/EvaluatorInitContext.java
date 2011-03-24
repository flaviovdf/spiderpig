package br.ufmg.dcc.vod.ncrawler.distributed.commune.client.synccontrol;

import br.edu.ufcg.lsd.commune.container.control.ServerModuleController;
import br.edu.ufcg.lsd.commune.container.servicemanager.client.InitializationContext;
import br.edu.ufcg.lsd.commune.container.servicemanager.client.sync.SyncManagerClient;

public class EvaluatorInitContext implements InitializationContext<ServerModuleController, SyncManagerClient<ServerModuleController>> {

	public static final String EVAL_MODULE = "EVAL_MODULE";

	@Override
	public SyncManagerClient<ServerModuleController> createManagerClient() {
		return new SyncManagerClient<ServerModuleController>();
	}

	@Override
	public Class<ServerModuleController> getManagerObjectType() {
		return ServerModuleController.class;
	}

	@Override
	public String getServerContainerName() {
		return EVAL_MODULE;
	}
	
}
