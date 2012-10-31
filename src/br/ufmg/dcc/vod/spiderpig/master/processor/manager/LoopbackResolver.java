package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

public class LoopbackResolver implements Resolver {

	private final JobExecutor jobExecutor;

	public LoopbackResolver(JobExecutor jobExecutor) {
		this.jobExecutor = jobExecutor;
	}

	@Override
	public JobExecutor resolve(ServiceID sid) {
		return this.jobExecutor;
	}

}
