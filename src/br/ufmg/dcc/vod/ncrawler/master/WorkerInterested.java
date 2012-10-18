package br.ufmg.dcc.vod.ncrawler.master;

import java.util.List;

public interface WorkerInterested {

	public void crawlDone(String id, List<String> toQueue);
	public void crawlError(String id, String cause);
	
}
