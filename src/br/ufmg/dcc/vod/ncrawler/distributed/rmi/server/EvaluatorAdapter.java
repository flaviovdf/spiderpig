package br.ufmg.dcc.vod.ncrawler.distributed.rmi.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Constants;
import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.EvaluatorProxy;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

/**
 * Adapter object to contact remote evaluators. Only the {@code evaluteAndSave}
 * and {@code error} methods are supported.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 *
 * @param <I> IDs of content to collect
 * @param <C> Content to collect
 */
public class EvaluatorAdapter<I, C> implements Evaluator<I, C>, Serializable {

	private static final long serialVersionUID = Constants.SERIAL_UID;
	
	private final EvaluatorProxy<I, C> client;

	/**
	 * Creates a new adapter which will decorate the given proxy. The proxy
	 * is a remote interface if a real {@code Evaluator} behind it.
	 * 
	 * @param proxy Proxy to contact
	 */
	public EvaluatorAdapter(EvaluatorProxy<I, C> client) {
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
	public Collection<CrawlJob<I, C>> getInitialCrawl() {
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
	public void setTrackerFactory(TrackerFactory<I> factory) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void ignore(Collection<I> ignore) {
		throw new UnsupportedOperationException();
	}
}
