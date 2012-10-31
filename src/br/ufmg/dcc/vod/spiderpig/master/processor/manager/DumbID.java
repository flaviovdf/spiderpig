package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

public class DumbID implements Resolver {

	private final JobExecutor jobExecutor;

	public DumbID(JobExecutor jobExecutor) {
		this.jobExecutor = jobExecutor;
	}

	@Override
	public JobExecutor resolve() {
		return this.jobExecutor;
	}

	@Override
	public ServiceID getWorkerID() {
		return null;
	}

}
