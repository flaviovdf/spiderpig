package br.ufmg.dcc.vod.ncrawler.jobs;

import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.master.WorkerInterested;

public interface JobExecutor {

	public void crawl(String id, WorkerInterested interested, FileSaver fsaver);
	
}
