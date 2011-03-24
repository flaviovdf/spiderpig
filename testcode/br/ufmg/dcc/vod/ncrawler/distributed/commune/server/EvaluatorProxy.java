package br.ufmg.dcc.vod.ncrawler.distributed.commune.server;

import java.io.Serializable;
import java.util.Collection;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.distributed.commune.client.EvaluatorClient;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

public class EvaluatorProxy<I, C> implements Evaluator<I, C>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private final EvaluatorClient<I, C> client;

	public EvaluatorProxy(EvaluatorClient<I, C> client) {
		this.client = client;
	}
	
	// Remote Methods
	@Override
	public void evaluteAndSave(I collectID, C collectContent) {
		this.client.evaluteAndSave(collectID, collectContent);
	}

	@Override
	public void error(I collectID, UnableToCollectException utc) {
		this.client.error(collectID, utc);
	}
	
	// Local Methods
	@Override
	public Collection<CrawlJob> getInitialCrawl() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setProcessor(Processor processor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStatsKeeper(StatsPrinter sp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTrackerFactory(TrackerFactory factory) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void ignore(Collection<I> ignore) {
		throw new UnsupportedOperationException();
	}
}
