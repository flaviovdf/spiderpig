package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import br.ufmg.dcc.vod.spiderpig.jobs.JobExecutor;

public class DumbID implements WorkerID {

	private final JobExecutor jobExecutor;

	public DumbID(JobExecutor jobExecutor) {
		this.jobExecutor = jobExecutor;
	}

	@Override
	public JobExecutor resolve() {
		return this.jobExecutor;
	}

}
