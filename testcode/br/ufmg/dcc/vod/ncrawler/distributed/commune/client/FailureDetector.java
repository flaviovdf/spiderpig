package br.ufmg.dcc.vod.ncrawler.distributed.commune.client;

import br.edu.ufcg.lsd.commune.api.Remote;
import br.edu.ufcg.lsd.commune.container.servicemanager.ServiceManager;

@Remote
public interface FailureDetector {

	public static final String NAME = "FAILURE_DETECTOR";

	public void init(ServiceManager m);
	
}
