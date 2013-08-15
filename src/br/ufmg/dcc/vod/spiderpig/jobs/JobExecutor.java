package br.ufmg.dcc.vod.spiderpig.jobs;

import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public interface JobExecutor {
	
	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver);
	
}
