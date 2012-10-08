package br.ufmg.dcc.vod.ncrawler.distributed.rmi.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.common.Constants;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

public class EvaluatorProxyImpl<I, C> extends UnicastRemoteObject implements
		EvaluatorProxy<I, C> {

	private static final long serialVersionUID = Constants.SERIAL_UID;

	private static final Logger LOG = Logger
			.getLogger(EvaluatorProxyImpl.class);

	// Volatile since it will not be serialized remotely
	private volatile Evaluator<I, C> e;

	public EvaluatorProxyImpl(int port) throws RemoteException {
		super(port);
	}

	// Remote method
	@Override
	public void evaluteAndSave(I collectID, C collectContent) {
		LOG.info("Result received: " + collectID);
		e.evaluteAndSave(collectID, collectContent);
	}

	@Override
	public void error(I collectID, UnableToCollectException utce)
			throws RemoteException {
		LOG.info("Result with error received" + collectID);
		e.error(collectID, utce);
	}
	
	// Local methods
	public void wrap(Evaluator<I, C> e) {
		this.e = e;
	}
}