package br.ufmg.dcc.vod.ncrawler.distributed.rmi.client;

import java.rmi.RemoteException;

import br.ufmg.dcc.vod.ncrawler.distributed.rmi.AbstractRMIBuilder;

/**
 * A RMI builder to construct and bind {@link EvaluatorProxy} objects.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 *
 * @param <I> Type of IDs to evaluate
 * @param <C> Type of content being crawled
 */
public class EvaluatorProxyBuilder<I, C> 
		extends AbstractRMIBuilder<EvaluatorProxy<I, C>> {

	/**
	 * Creates a new executor builder to bind at the given port.
	 * 
	 * @param port Port to use
	 * @throws RemoteException If network errors occur
	 */
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
