package br.ufmg.dcc.vod.spiderpig.master.walker;

import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public abstract class AbstractWalker extends AbstractConfigurable<Void> 
		implements ConfigurableWalker {

	private final StopCondition stopCondition;
	private Iterable<CrawlID> seeds;
	private ProcessorActor processorActor;

	public AbstractWalker() {
		this.stopCondition = createStopCondition();
	}
	
	private void dispatch(CrawlID crawlID) {
		this.processorActor.dispatch(crawlID);
	}
	
	@Override
	public final void setProcessorActor(ProcessorActor processorActor) {
		this.processorActor = processorActor;
	}
	
	@Override
	public final void dispatchNext(CrawlID crawled, Iterable<CrawlID> links) {
		Iterable<CrawlID> toWalk = getToWalkImpl(crawled, links);
		for (CrawlID id : toWalk) {
			dispatch(id);
			this.stopCondition.dispatched();
		}
		this.stopCondition.resultReceived();
	}

	@Override
	public final StopCondition getStopCondition() {
		return this.stopCondition;
	}
	
	@Override
	public final void errorReceived(CrawlID idWithError) {
		errorReceivedImpl(idWithError);
		this.stopCondition.errorReceived();
	}

	@Override
	public void workerFailedWithID(CrawlID id) {
		dispatch(id);
	}
	
	@Override
	public final void setSeeds(Iterable<CrawlID> seeds) {
		this.seeds = seeds;
	}
	
	@Override
	public final void dispatchSeeds() {
		Iterable<CrawlID> seedDispatch = filterSeeds(this.seeds);
		for (CrawlID id : seedDispatch) {
			dispatch(id);
			this.stopCondition.dispatched();
		}
	}
	
	protected abstract Iterable<CrawlID> filterSeeds(Iterable<CrawlID> seeds);
	
	protected abstract Iterable<CrawlID> getToWalkImpl(CrawlID crawled, 
			Iterable<CrawlID> links);
	
	protected abstract void errorReceivedImpl(CrawlID crawled);
	
	protected abstract StopCondition createStopCondition();
	
}
