package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.ExhaustCondition;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.tracker.BloomFilterTrackerFactory;
import br.ufmg.dcc.vod.spiderpig.tracker.Tracker;

/**
 * A walker based on breadth first search. A {@link Tracker} is used to keep
 * track of previously crawled ids.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class BFSWalker extends AbstractConfigurable<Void> 
		implements ConfigurableWalker {

	public static final String BLOOM_INSERTS = 
			"master.walkstrategy.bloomfilter_expected_inserts";
	
	private Tracker<String> tracker;
	private ArrayList<CrawlID> seed;
	private ExhaustCondition stopCondition;

	@Override
	public List<CrawlID> getToWalk(CrawlID crawled, List<CrawlID> links) {
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
	
	boolean wasCrawled(CrawlID id) {
		return this.tracker.wasCrawled(id.toString());
	}
	
	@Override
	public void addSeedID(CrawlID seed) {
		this.tracker.addCrawled(seed.getId());
		this.seed.add(seed);
	}

	@Override
	public List<CrawlID> getSeedDispatch() {
		return seed;
	}
	
	@Override
	public StopCondition getStopCondition() {
		return this.stopCondition;
	}
	
	@Override
	public Void realConfigurate(Configuration configuration) {
		int expectedInserts = configuration.getInt(BLOOM_INSERTS);
		this.tracker = new BloomFilterTrackerFactory<String>(expectedInserts)
							.createTracker(String.class);
		this.seed = new ArrayList<>();
		this.stopCondition = new ExhaustCondition();
		return null;
	}

	@Override
	public Set<String> getRequiredParameters() {
		return new HashSet<String>(Arrays.asList(BLOOM_INSERTS));
	}
}
