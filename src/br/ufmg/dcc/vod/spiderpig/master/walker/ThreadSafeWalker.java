package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public class ThreadSafeWalker implements ConfigurableWalker {

	private final ConfigurableWalker walker;
	private final ReentrantLock lock;

	public ThreadSafeWalker(ConfigurableWalker walker) {
		this.walker = walker;
		this.lock = new ReentrantLock();
	}
	
	@Override
	public void dispatchNext(CrawlID crawled, Iterable<CrawlID> links) {
		try {
			lock.lock();
			this.walker.dispatchNext(crawled, links);
		} finally {
			lock.unlock();
		}
	}


	@Override
	public void errorReceived(CrawlID idWithError) {
		try {
			lock.lock();
			this.walker.errorReceived(idWithError);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void setSeeds(Iterable<CrawlID> seeds) {
		try {
			lock.lock();
			this.walker.setSeeds(seeds);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void dispatchSeeds() {
		try {
			lock.lock();
			this.walker.dispatchSeeds();
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void workerFailedWithID(CrawlID id) {
		try {
			lock.lock();
			this.walker.workerFailedWithID(id);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void setProcessorActor(ProcessorActor processorActor) {
		try {
			lock.lock();
			this.walker.setProcessorActor(processorActor);
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

	@Override
	public StopCondition getStopCondition() {
		return this.walker.getStopCondition();
	}
}