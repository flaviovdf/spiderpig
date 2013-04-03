package br.ufmg.dcc.vod.spiderpig.jobs.youtube.rss;

import java.net.URL;
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
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.Link;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;

public class RSSMonitorWalker extends AbstractConfigurable<Void> 
	implements ConfigurableWalker {

	private static final Logger LOG = Logger.getLogger(RSSMonitorWalker.class);
	
	public static final String MAX_MONITOR = 
			"master.walkstrategy.youtube.rss.max";
	public static final String TIME_BETWEEN = 
			"master.walkstrategy.youtube.rss.time";
	public static final String FEED = 
			"master.walkstrategy.youtube.rss.feed";
	
	private int maxMonitor;
	private long timeBetween;
	private String feed;
	
	private HashSet<CrawlID> memorySet;
	private List<CrawlID> toCrawlList;
	
	private ThroughputManager throughputManager;
	private Requester<List<CrawlID>> requester;
	
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
						this.feed, this.requester);
				
				this.memorySet.addAll(links);
				this.toCrawlList = ImmutableList.copyOf(this.memorySet);
			} catch (Exception e) {
				LOG.error("Unable to parse feed " + this.feed, e);
			}
		}
		
		return this.toCrawlList;
	}

	@Override
	public void addSeedID(CrawlID seed) {
	}

	@Override
	public List<CrawlID> getSeedDispatch() {
		try {
			return this.throughputManager.sleepAndPerform(
					this.feed, this.requester);
		} catch (Exception e) {
			LOG.error("Unable to dispatch seed " + this.feed, e);
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Set<String> getRequiredParameters() {
		return new HashSet<String>(Arrays.asList(MAX_MONITOR, TIME_BETWEEN,
				FEED));
	}

	@Override
	public Void realConfigurate(Configuration configuration) throws Exception {
		
		this.maxMonitor = configuration.getInt(MAX_MONITOR);
		this.timeBetween = configuration.getLong(TIME_BETWEEN);
		this.throughputManager = new ThroughputManager(this.timeBetween);
		
		this.feed = configuration.getString(FEED);
		this.requester = new FeedRequester();
		
		return null;
	}
	
	private static class FeedRequester implements Requester<List<CrawlID>> {
		@Override
		public List<CrawlID> performRequest(String feed) throws Exception {
			
			List<CrawlID> returnVal = new ArrayList<>();
			YouTubeService service = new YouTubeService("");
			
			URL feedLink = new URL(feed);
			while (feedLink != null) {
				VideoFeed videoFeed = service.getFeed(feedLink, 
						VideoFeed.class);
				
				for (VideoEntry entry : videoFeed.getEntries()) {
					String[] vidIdSplit = entry.getId().split(":");
					String vidId = vidIdSplit[vidIdSplit.length - 1];
					returnVal.add(CrawlID.newBuilder().setId(vidId).build());
				}
					
				Link nextLink = videoFeed.getLink("next", 
                		"application/atom+xml");
				if (nextLink != null) {
					feedLink = new URL(nextLink.getHref());
				} else {
					feedLink = null;
				}
			}

			return returnVal;
		}
	}
}