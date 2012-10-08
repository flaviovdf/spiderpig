package br.ufmg.dcc.vod.ncrawler.distributed.rmi.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Objects;

import com.google.common.base.Preconditions;

import br.ufmg.dcc.vod.ncrawler.distributed.rmi.server.JobExecutor;

/**
 * Class which identifies collect servers.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class ServerID {

	private final String hostname;
	private final int port;
	
	private JobExecutor lookup;

	/**
	 * Creates a new collect server identification
	 * 
	 * @param hostname Host of the server
	 * @param port Port which is bound to
	 */
	public ServerID(String hostname, int port) {
		Preconditions.checkNotNull(hostname);
		
		this.hostname = hostname;
		this.port = port;
	}

	/**
	 * @return Hostname as string
	 */
	public String getHostname() {
		return hostname;
	}
	
	/**
	 * @return Port as integer
	 */
	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return hostname + ":" + port;
	}
	
	/**
	 * Looks up the remote reference associated with this id.
	 * If a reference was already looked up, returns it. If you desire
	 * to lookup reference again, call {@code reset}.
	 * 
	 * @return Remote ref
	 * 
	 * @throws RemoteException
	 * @throws NotBoundException
	 * @throws MalformedURLException
	 */
	public JobExecutor resolve() 
			throws RemoteException, NotBoundException, MalformedURLException {
		if (this.lookup == null) {
			this.lookup = (JobExecutor) Naming.lookup(
					"rmi://" + hostname + ":" + port + "/" + JobExecutor.NAME);
		}
		return lookup;
	}
	
	/**
	 * Sets the remote reference to null. 
	 */
	public void reset() {
		this.lookup = null;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(hostname, port);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ServerID) {
			ServerID other = (ServerID) obj;
			return hostname.equals(other.hostname) && port == other.port;
		}
		
		return false;
	}
}