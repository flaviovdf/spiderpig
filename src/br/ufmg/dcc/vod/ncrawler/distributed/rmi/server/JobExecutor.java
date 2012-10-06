package br.ufmg.dcc.vod.ncrawler.distributed.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;

public interface JobExecutor extends Remote {

	public static final String NAME = "EXECUTOR_SERVER";
	
	public void collect(CrawlJob c) throws RemoteException;

	public void kill() throws RemoteException;
	
}
