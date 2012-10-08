package br.ufmg.dcc.vod.ncrawler.distributed.rmi.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

/**
 * A {@code EvaluatorProxy} is a remote object used to contact 
 * {@link Evaluator} implementations.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 *
 * @param <I> Type of IDs to evaluate
 * @param <C> Type of content being crawled
 */
public interface EvaluatorProxy<I, C> extends Remote {

	public static final String NAME = "EVALUATOR_PROXY";

	/**
	 * Proxy method for {@link Evaluator.evaluateAndSave}
	 * 
	 * @param collectID
	 * @param collectContent
	 * 
	 * @throws RemoteException
	 */
	public void evaluteAndSave(I collectID, C collectContent) 
			throws RemoteException;
	
	public void error(I collectID, UnableToCollectException utce) 
			throws RemoteException;

}
