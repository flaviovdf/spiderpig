package br.ufmg.dcc.vod.ncrawler.distributed.commune.server;

import br.edu.ufcg.lsd.commune.api.Remote;
import br.ufmg.dcc.vod.ncrawler.CrawlJob;

@Remote
public interface JobExecutor {

	public static final String NAME = "EXECUTOR_SERVER";
	
	public void collect(CrawlJob c);

	public void kill();
	
}
