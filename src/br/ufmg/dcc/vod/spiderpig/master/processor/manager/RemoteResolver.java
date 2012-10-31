package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import java.util.Objects;

import br.ufmg.dcc.vod.spiderpig.distributed.master.JobExecutorProxy;
import br.ufmg.dcc.vod.spiderpig.distributed.nio.service.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

public class RemoteResolver implements Resolver {

	private final ServiceID workerID;
	private final ServiceID callBackID;
	private final ServiceID fileSaverID;
	private final RemoteMessageSender sender;

	public RemoteResolver(ServiceID workerID, ServiceID callBackID,
			ServiceID fileSaverID, RemoteMessageSender sender) {
		
		this.workerID = workerID;
		this.callBackID = callBackID;
		this.fileSaverID = fileSaverID;
		this.sender = sender;
	}

	@Override
	public JobExecutor resolve() {
		return new JobExecutorProxy(workerID, callBackID, fileSaverID, sender);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.workerID, this.workerID);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RemoteResolver))
			return false;
		
		RemoteResolver other = (RemoteResolver) obj;
		return Objects.equals(this.workerID, other.workerID);
	}

	@Override
	public ServiceID getWorkerID() {
		return this.workerID;
	}	
}
