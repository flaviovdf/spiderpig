package br.ufmg.dcc.vod.ncrawler.jobs;

import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;

public interface JobExecutor {

	public void crawl(String id, WorkerInterested interested, FileSaver saver);
	
}
