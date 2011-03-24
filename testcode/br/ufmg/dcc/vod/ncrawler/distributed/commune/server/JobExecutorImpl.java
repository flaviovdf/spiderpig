package br.ufmg.dcc.vod.ncrawler.distributed.commune.server;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.ui.EXIT_CODES;

public class JobExecutorImpl implements JobExecutor {

	private static final Logger LOG = Logger.getLogger(JobExecutorImpl.class);
	
	private static final long serialVersionUID = 1L;

	@Override
	public void collect(CrawlJob c) {
		LOG.info("Received JOB");
		c.collect();
		LOG.info("Finished JOB");
	}

	@Override
	public void kill()  {
		LOG.info("Exiting!");
		System.exit(EXIT_CODES.OK);
	}
	
}
