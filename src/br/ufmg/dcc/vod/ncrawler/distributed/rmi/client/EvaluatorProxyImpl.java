package br.ufmg.dcc.vod.ncrawler.distributed.rmi.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.common.Constants;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

/**
 * Implementation for the {@link EvaluatorProxy} interface. This is a decorator
 * for a {@code Evaluator} object.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 *
 * @param <I> Type of IDs to evaluate
 * @param <C> Type of content being crawled
 */
public class EvaluatorProxyImpl<I, C> extends UnicastRemoteObject implements
		EvaluatorProxy<I, C> {

	private static final long serialVersionUID = Constants.SERIAL_UID;

	private static final Logger LOG = Logger
			.getLogger(EvaluatorProxyImpl.class);

	// Volatile since it will not be serialized remotely
	private volatile Evaluator<I, C> evaluator;

	/**
	 * Creates a new proxy to bind at the given port.
	 * 
	 * @param port Proxy to use.
	 * 
	 * @throws RemoteException
	 */
	public EvaluatorProxyImpl(int port) throws RemoteException {
		super(port);
	}

	// Remote method
	@Override
	public void evaluteAndSave(I collectID, C collectContent) {
		LOG.info("Result received: " + collectID);
		evaluator.evaluteAndSave(collectID, collectContent);
	}

	@Override
	public void error(I collectID, UnableToCollectException utce)
			throws RemoteException {
		LOG.info("Result with error received" + collectID);
		evaluator.error(collectID, utce);
	}
	
	// Local methods
	/**
	 * Indicates the {@code Evaluator} which will be decorated.
	 * 
	 * @param evaluator Evaluator to decorate
	 */
	public void wrap(Evaluator<I, C> evaluator) {
		this.evaluator = evaluator;
	}
}