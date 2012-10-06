package br.ufmg.dcc.vod.ncrawler.distributed.rmi.client;

import java.rmi.RemoteException;

import br.ufmg.dcc.vod.ncrawler.distributed.rmi.AbstractRMIFactory;

public class EvaluatorClientFactory<I, C> extends AbstractRMIFactory<EvaluatorClientImpl<I, C>> {

	public EvaluatorClientFactory(int port) throws RemoteException {
		super(port);
	}
	
	@Override
	public EvaluatorClientImpl<I, C> create(int port) throws RemoteException {
		return new EvaluatorClientImpl<I, C>(port);
	}

	@Override
	public String getName() {
		return EvaluatorClient.NAME;
	}
}
