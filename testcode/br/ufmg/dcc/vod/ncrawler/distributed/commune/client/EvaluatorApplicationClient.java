package br.ufmg.dcc.vod.ncrawler.distributed.commune.client;

import br.edu.ufcg.lsd.commune.container.control.ServerModuleController;
import br.edu.ufcg.lsd.commune.container.servicemanager.client.InitializationContext;
import br.edu.ufcg.lsd.commune.container.servicemanager.client.sync.SyncApplicationClient;
import br.edu.ufcg.lsd.commune.container.servicemanager.client.sync.SyncManagerClient;
import br.edu.ufcg.lsd.commune.context.ModuleContext;
import br.edu.ufcg.lsd.commune.network.xmpp.CommuneNetworkException;
import br.edu.ufcg.lsd.commune.processor.ProcessorStartException;
import br.ufmg.dcc.vod.ncrawler.distributed.commune.client.synccontrol.EvaluatorInitContext;

public class EvaluatorApplicationClient extends SyncApplicationClient<ServerModuleController, SyncManagerClient<ServerModuleController>> {

	public static final String MODULE_NAME = "EVAL_APP_CLIENT";
	
	public EvaluatorApplicationClient(String containerName, ModuleContext context) throws CommuneNetworkException, ProcessorStartException {
		super(MODULE_NAME, context);
	}

	@Override
	protected InitializationContext<ServerModuleController, SyncManagerClient<ServerModuleController>> createInitializationContext() {
		return new EvaluatorInitContext();
	}
	
}
