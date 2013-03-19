package br.ufmg.dcc.vod.spiderpig.jobs.youtube.rss;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.jobs.Requester;
import br.ufmg.dcc.vod.spiderpig.jobs.ThroughputManager;
import br.ufmg.dcc.vod.spiderpig.master.walker.ConfigurableWalker;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.common.collect.ImmutableList;

public class RSSMonitorWalker extends AbstractConfigurable<Void> 
	implements ConfigurableWalker {

	private static final Logger LOG = Logger.getLogger(RSSMonitorWalker.class);
	
	public static final String MAX_MONITOR = "master.walkstrategy.rss.max";
	public static final String TIME_BETWEEN = "master.walkstrategy.rss.time";
	
	private HashSet<CrawlID> memorySet;
	private List<CrawlID> toCrawlList;
	private int maxMonitor;
	private CrawlID feed;
	private long timeBetween;
	private ThroughputManager throughputManager;
	
	public RSSMonitorWalker() {
		this.memorySet = new HashSet<>();
		this.toCrawlList = new ArrayList<>();
		this.maxMonitor = 0;
		this.feed = null;
		this.timeBetween = 0;
		this.throughputManager = null;
	}
	
	@Override
	public List<CrawlID> getToWalk(CrawlID crawled, List<CrawlID> links) {
		return getToWalk();
	}

	private List<CrawlID> getToWalk() {
		
		//adds new elements until it reaches totalMonitor
		if (this.toCrawlList.size() < this.maxMonitor) {
			
			try {
				List<CrawlID> links = this.throughputManager.sleepAndPerform(
						this.feed.getId(), new Requester<List<CrawlID>>() {
							@Override
							public List<CrawlID> performRequest(String crawlID)
									throws Exception {
								return null;
							}
						});
				
				this.memorySet.addAll(links);
				this.toCrawlList = ImmutableList.copyOf(this.memorySet);
			} catch (Exception e) {
				LOG.error("Unable to parse feed " + this.feed.getId(), e);
			}
		}
		
		return this.toCrawlList;
	}

	@Override
	public void addSeedID(CrawlID seed) {
		this.feed = seed;
	}

	@Override
	public Set<String> getRequiredParameters() {
		return new HashSet<String>(Arrays.asList(MAX_MONITOR, TIME_BETWEEN));
	}

	@Override
	public Void realConfigurate(Configuration configuration) throws Exception {
		
		this.maxMonitor = configuration.getInt(MAX_MONITOR);
		this.timeBetween = configuration.getLong(TIME_BETWEEN);
		this.throughputManager = new ThroughputManager(this.timeBetween);
		
//		FeedParser parser = 
//				(FeedParser) constructor.newInstance();
//		
//		this.parser = parser;
		
		return null;
	}
}