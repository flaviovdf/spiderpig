package br.ufmg.dcc.vod.ncrawler.distributed.commune.client.synccontrol;

import java.util.Set;

import br.edu.ufcg.lsd.commune.identification.ServiceID;

public interface EvaluatorManager {

	public static String NAME = "EVAL_MANAGER";
	
	public void setWorkers(Set<ServiceID> workers);
	
}
