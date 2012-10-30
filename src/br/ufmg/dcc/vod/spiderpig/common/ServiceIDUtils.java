package br.ufmg.dcc.vod.spiderpig.common;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

public class ServiceIDUtils {

	public static ServiceID toServiceID(String hostname, int port, 
			String handle) {
		ServiceID.Builder builder = ServiceID.newBuilder();
		return builder
				.setHostname(hostname).setPort(port).setHandle(handle).build();
	}
	
}
