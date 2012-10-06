package br.ufmg.dcc.vod.ncrawler.jobs.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;
import br.ufmg.dcc.vod.ncrawler.stats.CompositeStatEvent;
import br.ufmg.dcc.vod.ncrawler.stats.Display;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.Tracker;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

public abstract class AbstractEvaluator<C> implements Evaluator<String, C> {

	private static final Logger LOG = Logger.getLogger(AbstractEvaluator.class);
	
	private static final String DIS = "DIS";
	private static final String COL = "COL";
	private static final String ERR = "ERR";
	
	private Processor processor;
	private StatsPrinter sp;
	private Tracker<String> tracker;

	@Override
	public final void error(String collectID, UnableToCollectException e) {
		Map<String, Integer> incs = new HashMap<String, Integer>();
		incs.put(ERR, 1);
		sp.notify(new CompositeStatEvent(incs));
		LOG.error("Error collecting: " + collectID, e);
	}

	@Override
	public final void evaluteAndSave(String collectID, C collectContent) {
		try {
			Collection<String> next = realEvaluateAndSave(collectID, collectContent);
			Map<String, Integer> incs = new HashMap<String, Integer>();
			incs.put(COL, 1);
			sp.notify(new CompositeStatEvent(incs));
			LOG.info("Collected " + collectID);

			if (next != null) {
				Collection<CrawlJob> dispatch = createJobs(next);
				for (CrawlJob j : dispatch) {
					this.processor.dispatch(j);
				}
			}
		} catch (Exception e) {
			error(collectID, new UnableToCollectException(e.getMessage()));
		}
	}
	
	public abstract Collection<String> realEvaluateAndSave(String collectID, C collectContent) throws Exception;

	public abstract CrawlJob createJob(String next);
	
	@Override
	public final void setProcessor(Processor processor) {
		this.processor = processor;
	}
	
	@Override
	public final void setTrackerFactory(TrackerFactory factory) {
		this.tracker = factory.createTracker();
	}
	
	public Collection<CrawlJob> createJobs(Collection<String> next) {
		Collection<CrawlJob> rv = new ArrayList<CrawlJob>();
		Map<String, Integer> incs = new HashMap<String, Integer>();
		incs.put(DIS, 0);
		for (String n : next) {
			if (!tracker.contains(n)) {
				LOG.info("Discovered new ID " + n);
				
				CrawlJob createJob = createJob(n);
				tracker.add(n);
				incs.put(DIS, incs.get(DIS) + 1);
				rv.add(createJob);
			}
		}
		sp.notify(new CompositeStatEvent(incs));
		return rv;
	}
	
	@Override
	public final Collection<CrawlJob> getInitialCrawl() {
		Collection<String> seeds = getSeeds();
		if (seeds != null) {
			Collection<CrawlJob> dispatch = createJobs(seeds);
			return dispatch;
		} else {
			return new ArrayList<CrawlJob>();
		}
	}
	
	public abstract Collection<String> getSeeds();

	@Override
	public final void setStatsKeeper(StatsPrinter sp) {
		this.sp = sp;
		this.sp.setDisplay(new Display() {
			@Override
			public void print(Map<String, Integer> m) {
				Integer disU = m.get(DIS) == null ? 0 : m.get(DIS);
				Integer colU = m.get(COL) == null ? 0 : m.get(COL);
				Integer errU = m.get(ERR) == null ? 0 : m.get(ERR);
				
				System.out.println("----" + new Date());
				System.out.println(DIS + " : " + disU);
				System.out.println(COL + " : " + colU + " ( " + (colU.doubleValue() / disU.doubleValue()) + " )");
				System.out.println(ERR + " : " + errU + " ( " + (errU.doubleValue() / disU.doubleValue()) + " )");
			}
			}
		);
	}
	
	@Override
	public void ignore(Collection<String> ignore) {
		for (String i : ignore) {
			this.tracker.add(i);
		}
	}
}