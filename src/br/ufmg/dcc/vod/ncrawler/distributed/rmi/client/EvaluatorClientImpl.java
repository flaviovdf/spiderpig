package br.ufmg.dcc.vod.ncrawler.distributed.rmi.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

public class EvaluatorClientImpl<I, C> extends UnicastRemoteObject implements EvaluatorClient<I, C>  {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(EvaluatorClientImpl.class);
	
	// Volatile since it will not be serialized remotely
	private volatile Evaluator<I, C> e;

	public EvaluatorClientImpl(int port) throws RemoteException {
		super(port);
	}

	//Remote method
	@Override
	public void evaluteAndSave(I collectID, C collectContent) {
		LOG.info("Result received: "+ collectID);
		e.evaluteAndSave(collectID, collectContent);
	}
	
	//Local methods
	public void wrap(Evaluator<I, C> e) {
		this.e = e;
	}

	@Override
	public void error(I collectID, UnableToCollectException utce)
			throws RemoteException {
		LOG.info("Result with error received"+ collectID);
		e.error(collectID, utce);
	}
}