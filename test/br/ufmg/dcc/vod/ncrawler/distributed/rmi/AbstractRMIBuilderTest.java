package br.ufmg.dcc.vod.ncrawler.distributed.rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import junit.framework.Assert;

import org.junit.Test;

import br.ufmg.dcc.vod.ncrawler.common.Constants;

public class AbstractRMIBuilderTest {

	@Test
	public void testAll() throws Exception {
		AbstractRMIBuilder<SomeRemote> builder = 
				new AbstractRMIBuilder<SomeRemote>(9090) {
			
			@Override
			public String getName() {
				return "TEST";
			}
			
			@Override
			public SomeRemote create(int port) throws RemoteException {
				return new SomeRemoteImpl(port);
			}
		};
		
		builder.createAndBind();
		
		Registry registry = LocateRegistry.getRegistry(9090);
		Assert.assertNotNull(registry.lookup("TEST"));
		builder.shutdown();
		
		try {
			registry.lookup("TEST");
			Assert.fail();
		} catch (NoSuchObjectException e) {}
	}

	private interface SomeRemote extends Remote {
	}
	
	private class SomeRemoteImpl 
			extends UnicastRemoteObject implements SomeRemote {

		private static final long serialVersionUID = Constants.SERIAL_UID;
		
		protected SomeRemoteImpl(int port) throws RemoteException {
			super(port);
		}

		
	}
}
