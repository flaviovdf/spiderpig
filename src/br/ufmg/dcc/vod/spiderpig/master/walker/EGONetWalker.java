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
 * A walker based on ego networks. Basically, the first ego network of a node 
 * are the the nodes edges. The second ego network are the nodes edges plus
 * edges from nodes in the first ego net. The same concept applies to the
 * i-th ego network. Each network is crawled in a breadth first search manner. 
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class EGONetWalker extends AbstractConfigurable<Void> 
		implements ConfigurableWalker {

	public static final String NUM_NETS = "master.walkstrategy.ego.nets";
	
	private Tracker<String>[] trackers;
	private ArrayList<CrawlID> seed;
	private ExhaustCondition stopCondition;

	@Override
	public List<CrawlID> getToWalk(CrawlID crawled, List<CrawlID> links) {
		
		int crawlIDLayer = -1;
		for (int i = 0; i < trackers.length; i++) {
			if (this.trackers[i].wasCrawled(crawled.getId())) {
				crawlIDLayer = i;
				break;
			}
		}
		
		if (crawlIDLayer == -1)
			throw new RuntimeException("Unable to find " + crawled + 
					". This should only happen if seed was not added");
		
		List<CrawlID> rv = new ArrayList<>();
		if (crawlIDLayer < trackers.length - 1 && links != null)
			for (CrawlID link : links) {
				
				boolean linkPrevCrawled = false;
				for (int i = 0; i < trackers.length; i++) {
					if (this.trackers[i].wasCrawled(link.getId())) {
						linkPrevCrawled = true;
						break;
					}
				}
				
				boolean added = 
						this.trackers[crawlIDLayer + 1]
								.addCrawled(link.getId());
				if (added && !linkPrevCrawled)
					rv.add(link);
			}
			
		return rv;
	}

	@Override
	public void addSeedID(CrawlID seed) {
		this.trackers[0].addCrawled(seed.getId());
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
	
	@SuppressWarnings("unchecked")
	@Override
	public Void realConfigurate(Configuration configuration) {
		
		int numEgoNets = configuration.getInt(NUM_NETS) + 1;
		
		int expectedInserts = configuration.getInt(BFSWalker.BLOOM_INSERTS);
		BloomFilterTrackerFactory<String> factory = 
				new BloomFilterTrackerFactory<String>(expectedInserts);
		
		this.trackers = new Tracker[numEgoNets];
		for (int i = 0; i < numEgoNets; i++)
			this.trackers[i] = factory.createThreadSafeTracker(String.class);
		
		this.seed = new ArrayList<>();
		this.stopCondition = new ExhaustCondition();
		return null;
	}

	@Override
	public Set<String> getRequiredParameters() {
		return new HashSet<String>(Arrays.asList(BFSWalker.BLOOM_INSERTS, 
				NUM_NETS));
	}
}