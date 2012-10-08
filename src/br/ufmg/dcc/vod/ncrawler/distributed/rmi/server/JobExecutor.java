package br.ufmg.dcc.vod.ncrawler.distributed.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;

/**
 * Each server exports a {@code JobExecutor} for collecting data. This class
 * will execute the a {@code CrawlJob}.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface JobExecutor extends Remote {

	public static final String NAME = "EXECUTOR_SERVER";
	
	/**
	 * Execute the given job remotely
	 * 
	 * @param crawlJob Job to execute
	 * 
	 * @throws RemoteException If network errors occur
	 */
	public void collect(CrawlJob<?, ?> crawlJob) throws RemoteException;

	/**
	 * Kills this executor.
	 * 
	 * TODO: Not sure if we should keep this here.
	 * 
	 * @throws RemoteException  If network errors occur
	 */
	public void kill() throws RemoteException;
	
}