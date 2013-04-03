package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public class ThreadSafeWalker implements ConfigurableWalker {

	private final ConfigurableWalker walker;
	private final ReentrantLock lock;

	public ThreadSafeWalker(ConfigurableWalker walker) {
		this.walker = walker;
		this.lock = new ReentrantLock();
	}
	
	@Override
	public List<CrawlID> getToWalk(CrawlID crawled, List<CrawlID> links) {
		try {
			lock.lock();
			return this.walker.getToWalk(crawled, links);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void addSeedID(CrawlID seed) {
		try {
			lock.lock();
			this.walker.addSeedID(seed);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<CrawlID> getSeedDispatch() {
		try {
			lock.lock();
			return this.walker.getSeedDispatch();
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public Void configurate(Configuration configuration) throws Exception {
		return this.walker.configurate(configuration);
	}

	@Override
	public Set<String> getRequiredParameters() {
		return this.walker.getRequiredParameters();
	}
}