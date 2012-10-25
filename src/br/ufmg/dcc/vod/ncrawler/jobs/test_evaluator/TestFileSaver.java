package br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;

public class TestFileSaver implements FileSaver {

	private Map<Integer, byte[]> crawled = 
			Collections.synchronizedMap(new HashMap<Integer, byte[]>());
	
	@Override
	public void save(String fileID, byte[] payload) {
		this.crawled.put(Integer.parseInt(fileID), payload);
	}

	public Map<Integer, byte[]> getCrawled() {
		return crawled;
	}

}
