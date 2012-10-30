package br.ufmg.dcc.vod.ncrawler.master.processor.manager;

import java.util.Objects;

import br.ufmg.dcc.vod.ncrawler.distributed.master.JobExecutorProxy;
import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.RemoteMessageSender;
import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Ids.ServiceID;

public class RemoteWorkerID implements WorkerID {

	private final ServiceID workerID;
	private final ServiceID callBackID;
	private final ServiceID fileSaverID;
	private final RemoteMessageSender sender;

	public RemoteWorkerID(ServiceID workerID, ServiceID callBackID,
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
		if (!(obj instanceof RemoteWorkerID))
			return false;
		
		RemoteWorkerID other = (RemoteWorkerID) obj;
		return Objects.equals(this.workerID, other.workerID);
	}	
}
