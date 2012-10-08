package br.ufmg.dcc.vod.ncrawler.distributed.rmi.client;

import java.rmi.RemoteException;

import br.ufmg.dcc.vod.ncrawler.distributed.rmi.AbstractRMIBuilder;

public class EvaluatorProxyBuilder<I, C> 
		extends AbstractRMIBuilder<EvaluatorProxy<I, C>> {

	public EvaluatorProxyBuilder(int port) throws RemoteException {
		super(port);
	}
	
	@Override
	public EvaluatorProxy<I, C> create(int port) throws RemoteException {
		return new EvaluatorProxyImpl<I, C>(port);
	}

	@Override
	public String getName() {
		return EvaluatorProxy.NAME;
	}
}
