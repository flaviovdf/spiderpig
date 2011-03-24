package br.ufmg.dcc.vod.ncrawler.distributed.rmi.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import br.ufmg.dcc.vod.ncrawler.distributed.rmi.server.JobExecutor;

public class ServerID {

	private final String hostname;
	private final int port;
	
	private JobExecutor lookup;

	public ServerID(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}

	public String getHostname() {
		return hostname;
	}
	
	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return hostname + ":" + port;
	}
	
	public JobExecutor resolve() throws RemoteException, NotBoundException, MalformedURLException {
		if (this.lookup == null) {
			this.lookup = (JobExecutor) Naming.lookup("rmi://" + hostname + ":" + port + "/" + JobExecutor.NAME);
		}
		return lookup;
	}
	
	public void reset() {
		this.lookup = null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerID other = (ServerID) obj;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
	
}