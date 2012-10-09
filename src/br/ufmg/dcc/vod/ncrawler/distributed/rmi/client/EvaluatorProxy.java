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
	 * Proxy method for {@link Evaluator.evaluateAndSave}. Sends to the
	 * evaluator the crawled content associated with this id.
	 * 
	 * @param collectID ID of the collected content
	 * @param collectContent Content which was collected
	 * 
	 * @throws RemoteException
	 */
	public void evaluteAndSave(I collectID, C collectContent) 
			throws RemoteException;
	
	/**
	 * Proxy method for {@link Evaluator.error}. Indicates that an 
	 * error occurred while collecting the {@code collectID}
	 * 
	 * @param collectID ID which had errors
	 * @param utce Exception with details on the error
	 * 
	 * @throws RemoteException
	 */
	public void error(I collectID, UnableToCollectException utce) 
			throws RemoteException;

}
