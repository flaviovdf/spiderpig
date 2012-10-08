package br.ufmg.dcc.vod.ncrawler.distributed.rmi.failure_detector;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Failure detection interface. Our failure detection scheme is very simple,
 * basically nodes are suspected if they don't respond a {@code ping} with a
 * {@code pong}. We suggest that remote exception be ignored.
 *  
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface FailureDetector extends Remote {

	/**
	 * Send's a ping indicating the callback object.
	 * 
	 * @param handle Long value to identify the ping
	 * @param callback Remote object which will receive callback
	 * @throws RemoteException If any network errors occur.
	 */
	public void ping(long handle, FailureDetector callback) 
			throws RemoteException;
	
	/**
	 * Indicates that a {@code ping} with the given handle was received.
	 * 
	 * @param handle Identification of the ping
	 * @throws RemoteException If any network errors occur.
	 */
	public void pong(long handle) throws RemoteException;
	
}
