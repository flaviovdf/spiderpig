package br.ufmg.dcc.vod.ncrawler.distributed.rmi.server;

import java.rmi.RemoteException;

import br.ufmg.dcc.vod.ncrawler.distributed.rmi.AbstractRMIBuilder;

public class JobExecutorFactory extends AbstractRMIBuilder<JobExecutor> {

	public JobExecutorFactory(int port) throws RemoteException {
		super(port);
	}
	
	@Override
	public JobExecutor create(int port) throws RemoteException {
		return new JobExecutorImpl(port);
	}

	@Override
	public String getName() {
		return JobExecutor.NAME;
	}
	
}
