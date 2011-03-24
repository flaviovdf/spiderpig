package br.ufmg.dcc.vod.ncrawler.distributed.rmi.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

public interface EvaluatorClient<I, C> extends Remote {

	public static final String NAME = "EVAL_CLIENT";

	public void evaluteAndSave(I collectID, C collectContent) throws RemoteException;
	
	public void error(I collectID, UnableToCollectException utce) throws RemoteException;

}
