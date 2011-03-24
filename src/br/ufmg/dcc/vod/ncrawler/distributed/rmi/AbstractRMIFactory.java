package br.ufmg.dcc.vod.ncrawler.distributed.rmi;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import sun.rmi.registry.RegistryImpl;

public abstract class AbstractRMIFactory<T extends Remote> {

	private final int port;
	private Registry ri;
	private T create;

	public AbstractRMIFactory(int port) throws RemoteException {
		this.port = port;
		this.ri = new RegistryImpl(port);
	}
	
	public final T createAndBind() throws RemoteException, AlreadyBoundException {
		this.create = create(port);
		ri.bind(getName(), create);
		return create;
	}

	public final void shutdown() throws AccessException, RemoteException, NotBoundException {
		ri.unbind(getName());
		UnicastRemoteObject.unexportObject(create, true);
		UnicastRemoteObject.unexportObject(ri, true);
		create = null;
		ri = null;
	}

	public abstract String getName();
	
	public abstract T create(int port) throws RemoteException;
}
