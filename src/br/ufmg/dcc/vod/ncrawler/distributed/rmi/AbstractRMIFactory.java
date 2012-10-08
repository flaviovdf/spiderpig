package br.ufmg.dcc.vod.ncrawler.distributed.rmi;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Helper object to create and Bind RMI objects.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 *
 * @param <T> Type f object to create
 */
public abstract class AbstractRMIFactory<T extends Remote> {

	private final int port;
	private Registry ri;
	private T create;

	/**
	 * Creates a new Factory which will bind objects at the RMI registry in the
	 * given port.
	 * 
	 * @param port Port to use
	 * @throws RemoteException If registry cannot be created / located.
	 */
	public AbstractRMIFactory(int port) throws RemoteException {
		this.port = port;
		this.ri = LocateRegistry.createRegistry(port);
	}
	
	/**
	 * Create and Bind the object
	 * 
	 * @return The created Remote object
	 * 
	 * @throws RemoteException If cannot bind
	 * @throws AlreadyBoundException If cannot bind
	 */
	public final T createAndBind() 
			throws RemoteException, AlreadyBoundException {
		
		this.create = create(port);
		this.ri.bind(getName(), create);
		return create;
	}

	public final void shutdown() 
			throws AccessException, RemoteException, NotBoundException {
		
		this.ri.unbind(getName());
		UnicastRemoteObject.unexportObject(this.create, true);
		UnicastRemoteObject.unexportObject(this.ri, true);
		this.create = null;
		this.ri = null;
	}

	/**
	 * Return the name which the object will be bound to
	 * 
	 * @return Name to bind
	 */
	public abstract String getName();
	
	/**
	 * Creates the remote object
	 * 
	 * @param port
	 * @return
	 * @throws RemoteException
	 */
	public abstract T create(int port) throws RemoteException;
}
