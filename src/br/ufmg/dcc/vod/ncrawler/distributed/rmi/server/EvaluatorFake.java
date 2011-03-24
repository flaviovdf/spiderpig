package br.ufmg.dcc.vod.ncrawler.distributed.rmi.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.EvaluatorClient;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

public class EvaluatorFake<I, C> implements Evaluator<I, C>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private final EvaluatorClient<I, C> client;

	public EvaluatorFake(EvaluatorClient<I, C> client) {
		this.client = client;
	}
	
	// Remote Methods
	@Override
	public void evaluteAndSave(I collectID, C collectContent) {
		try {
			this.client.evaluteAndSave(collectID, collectContent);
		} catch (RemoteException re) {
			throw new RuntimeException(re);
		}
	}

	@Override
	public void error(I collectID, UnableToCollectException utc) {
		try {
			this.client.error(collectID, utc);
		} catch (RemoteException re) {
			throw new RuntimeException(re);
		}
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
