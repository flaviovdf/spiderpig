package br.ufmg.dcc.vod.ncrawler.evaluator;

import java.util.Collection;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

public interface Evaluator<I, C> {
	
	public void setTrackerFactory(TrackerFactory factory);
	public void ignore(Collection<I> ignore);
	
	public void setStatsKeeper(StatsPrinter sp);
	public void setProcessor(Processor processor);
	
	public Collection<CrawlJob> getInitialCrawl();
	
	public void evaluteAndSave(I collectID, C collectContent);
	public void error(I collectID, UnableToCollectException utc);
	
}