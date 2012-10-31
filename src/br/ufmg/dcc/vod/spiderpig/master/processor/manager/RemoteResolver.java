package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import java.util.Objects;

import br.ufmg.dcc.vod.spiderpig.distributed.master.JobExecutorProxy;
import br.ufmg.dcc.vod.spiderpig.distributed.nio.service.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

public class RemoteResolver implements Resolver {

	private final ServiceID callBackID;
	private final ServiceID fileSaverID;
	private final RemoteMessageSender sender;

	public RemoteResolver(ServiceID callBackID,
			ServiceID fileSaverID, RemoteMessageSender sender) {
		
		this.callBackID = callBackID;
		this.fileSaverID = fileSaverID;
		this.sender = sender;
	}

	@Override
	public JobExecutor resolve(ServiceID workerID) {
		return new JobExecutorProxy(workerID, callBackID, fileSaverID, sender);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.callBackID, this.fileSaverID);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RemoteResolver))
			return false;
		
		RemoteResolver other = (RemoteResolver) obj;
		return Objects.equals(this.callBackID, other.callBackID) &&
				Objects.equals(this.fileSaverID, other.fileSaverID);
	}
}