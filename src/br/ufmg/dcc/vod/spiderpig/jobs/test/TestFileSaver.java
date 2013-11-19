package br.ufmg.dcc.vod.spiderpig.jobs.test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;

public class TestFileSaver implements FileSaver {

	private Map<Integer, byte[]> crawled = 
			Collections.synchronizedMap(new HashMap<Integer, byte[]>());
	private AtomicInteger saved = new AtomicInteger(0);
	private final int expectedCalls;
	
	public TestFileSaver(int expectedCalls) {
		this.expectedCalls = expectedCalls;
	}

	@Override
	public synchronized void save(String fileID, byte[] payload) {
		this.crawled.put(Integer.parseInt(fileID), payload);
		this.saved.incrementAndGet();
	}

	public Map<Integer, byte[]> getCrawled() {
		return crawled;
	}

	public boolean isConsistent() {
		System.out.println(this.saved.get());
		return this.saved.get() == this.expectedCalls;
	}
	
	@Override
	public int numSaved() {
		return this.saved.get();
	}

	@Override
	public boolean close() throws IOException {
		return true;
	}

}
