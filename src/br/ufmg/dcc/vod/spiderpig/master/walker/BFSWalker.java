package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.VoidArguments;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.tracker.BloomFilterTrackerFactory;
import br.ufmg.dcc.vod.spiderpig.tracker.Tracker;

/**
 * A walker based on breadth first search. A {@link Tracker} is used to keep
 * track of previously crawled ids.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class BFSWalker implements ConfigurableWalker {

	public static final String BLOOM_INSERTS = 
			"master.walkstrategy.bfs.bloomfilter_expected_inserts";
	
	private Tracker<String> tracker;

	@Override
	public synchronized List<CrawlID> getToWalk(CrawlID crawled, 
			List<CrawlID> links) {
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
	public synchronized void addSeedID(CrawlID seed) {
		this.tracker.addCrawled(seed.getId());
	}

	@Override
	public synchronized VoidArguments configurate(Configuration configuration) {
		int expectedInserts = configuration.getInt(BLOOM_INSERTS);
		this.tracker = new BloomFilterTrackerFactory<String>(expectedInserts)
							.createTracker(String.class);
		
		return null;
	}
}
