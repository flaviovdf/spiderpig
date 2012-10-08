package br.ufmg.dcc.vod.ncrawler.distributed.rmi.server;

import java.rmi.RemoteException;

import br.ufmg.dcc.vod.ncrawler.distributed.rmi.AbstractRMIBuilder;

/**
 * Builds and binds new job executors.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class JobExecutorBuilder extends AbstractRMIBuilder<JobExecutor> {

	/**
	 * Creates a new executor builder to bind at the given port.
	 * 
	 * @param port Port to use
	 * @throws RemoteException If network errors occur
	 */
	public JobExecutorBuilder(int port) throws RemoteException {
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