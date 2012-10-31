package br.ufmg.dcc.vod.spiderpig.distributed.fd;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

public interface FDListener {

	public void isUp(ServiceID serviceID);
	
	public void isSuspected(ServiceID serviceID);
	
}
