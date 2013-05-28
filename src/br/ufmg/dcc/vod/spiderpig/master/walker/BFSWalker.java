package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.ExhaustCondition;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.master.walker.tracker.BloomFilterTrackerFactory;
import br.ufmg.dcc.vod.spiderpig.master.walker.tracker.Tracker;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

/**
 * A walker based on breadth first search. A {@link Tracker} is used to keep
 * track of previously crawled ids.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class BFSWalker extends AbstractWalker {

	public static final String BLOOM_INSERTS = 
			"master.walkstrategy.bloomfilter_expected_inserts";
	
	private Tracker<String> tracker;

	@Override
	protected List<CrawlID> getToWalkImpl(CrawlID crawled, List<CrawlID> links) {
		List<CrawlID> rv = new ArrayList<>();

		if (!tracker.wasCrawled(crawled.getId())) {
			throw new RuntimeException("ID not foun in tracker. This can" +
					" only happen if id was not signalled as seed");
		}
		
		if (links != null)
			for (CrawlID link : links) {
				if (this.tracker.addCrawled(link.getId())) { 
					rv.add(link);
				}
			}
		
		return rv;
	}
	
	
	@Override
	protected List<CrawlID> filterSeeds(List<CrawlID> seeds) {
		List<CrawlID> toDispatch = new ArrayList<>();
		for (CrawlID seed : seeds)
			if (tracker.addCrawled((seed.getId())))
				toDispatch.add(seed);
		
		return toDispatch;
	}

	@Override
	protected void errorReceivedImpl(CrawlID crawled) {
		tracker.wasCrawled(crawled.getId());
	}

	@Override
	protected StopCondition createStopCondition() {
		return new ExhaustCondition();
	}
	
	@Override
	public Void realConfigurate(Configuration configuration) {
		int expectedInserts = configuration.getInt(BLOOM_INSERTS);
		this.tracker = new BloomFilterTrackerFactory<String>(expectedInserts)
							.createTracker(String.class);
		return null;
	}

	@Override
	public Set<String> getRequiredParameters() {
		return new HashSet<String>(Arrays.asList(BLOOM_INSERTS));
	}
}
