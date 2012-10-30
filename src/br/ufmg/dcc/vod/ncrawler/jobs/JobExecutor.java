package br.ufmg.dcc.vod.ncrawler.jobs;

import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Ids.CrawlID;

public interface JobExecutor {

	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver);
	
}
