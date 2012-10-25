package br.ufmg.dcc.vod.ncrawler.master.processor.manager;

import java.util.Objects;

import br.ufmg.dcc.vod.ncrawler.distributed.master.JobExecutorProxy;
import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;

public class RemoteWorkerID implements WorkerID {

	private final String receiverHost;
	private final int receiverPort;
	
	private final String callBackHost;
	private final int callBackPort;
	
	private final String fileSaverHost;
	private final int fileSaverPort;
	
	public RemoteWorkerID(String receiverHost, int receiverPort,
			String callBackHost, int callBackPort, String fileSaverHost,
			int fileSaverPort) {
		
		this.receiverHost = receiverHost;
		this.receiverPort = receiverPort;
		this.callBackHost = callBackHost;
		this.callBackPort = callBackPort;
		this.fileSaverHost = fileSaverHost;
		this.fileSaverPort = fileSaverPort;
	}

	@Override
	public JobExecutor resolve() {
		return new JobExecutorProxy(receiverHost, receiverPort, 
				callBackHost, callBackPort, fileSaverHost, fileSaverPort);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.receiverHost, this.receiverPort);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RemoteWorkerID))
			return false;
		
		RemoteWorkerID other = (RemoteWorkerID) obj;
		return Objects.equals(this.receiverHost, other.receiverHost) &&
				Objects.equals(this.receiverPort, other.receiverPort);
	}	
}
