package br.ufmg.dcc.vod.ncrawler.master.processor.manager;

import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;

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
