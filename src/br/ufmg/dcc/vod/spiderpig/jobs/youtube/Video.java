package br.ufmg.dcc.vod.spiderpig.jobs.youtube;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.common.Tuple;
import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableJobExecutor;
import br.ufmg.dcc.vod.spiderpig.jobs.Requester;
import br.ufmg.dcc.vod.spiderpig.jobs.ThroughputManager;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.jobs.youtube.api.VideoAPIRequester;
import br.ufmg.dcc.vod.spiderpig.jobs.youtube.html.HTMLPageRequester;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.common.collect.Sets;

public class Video extends AbstractConfigurable<Void> 
		implements ConfigurableJobExecutor {

	private static final Logger LOG = Logger.getLogger(Video.class);
	
	private static final String SLEEP_TIME = "worker.job.youtube.sleeptime";
	private static final String DEV_KEY = "worker.job.youtube.devkey";
	private static final String APP_NAME = "worker.job.youtube.appname";
	
	private static final String CRAWL_HTML = "worker.job.youtube.html";
	private static final String CRAWL_STATS = "worker.job.youtube.stats";
	private static final String CRAWL_API = "worker.job.youtube.api";
	
	private ThroughputManager throughputManager;
	private MultiRequester requester;

	
	@Override
	public Set<String> getRequiredParameters() {
		return Sets.newHashSet(SLEEP_TIME, DEV_KEY, APP_NAME, CRAWL_HTML,
				CRAWL_STATS, CRAWL_API);
	}

	@Override
	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver) {
		LOG.info("Received id " + id + " " + id.getId());
		try {
			List<Tuple<String, byte[]>> result = 
					this.throughputManager.sleepAndPerform(id.getId(), 
							requester);
			
			for (Tuple<String, byte[]> t : result) {
				String type = id.getResourceType();
				if (type.length() > 0)
					saver.save(id.getResourceType() + "-" + t.first, t.second);
				else
					saver.save(t.first, t.second);
			}
			interested.crawlDone(id, null);
			LOG.info("Sending result for id " + id);
		} catch (Exception e) {
			LOG.error("Error at id " + id, e);
			interested.crawlError(id, e.toString());
		}
	}

	@Override
	public Void realConfigurate(Configuration configuration) throws Exception {
		long timeBetweenRequests = configuration.getLong(SLEEP_TIME);
		String devKey = configuration.getString(DEV_KEY);
		String appName = configuration.getString(APP_NAME);
		
		boolean crawlHtml = configuration.getBoolean(CRAWL_HTML);
		boolean crawlStats = configuration.getBoolean(CRAWL_STATS);
		boolean crawlApi = configuration.getBoolean(CRAWL_API);
		
		boolean hasOne = crawlHtml || crawlStats || crawlApi;
		if (!hasOne)
			throw new ConfigurationException("Please set at least one option"
					+ " to crawl");
		
		this.throughputManager = new ThroughputManager(timeBetweenRequests);
		this.requester = new MultiRequester(appName, devKey, crawlHtml, 
				crawlStats, crawlApi);
		
		return null;
	}
	
	private class MultiRequester implements 
			Requester<List<Tuple<String, byte[]>>> {
		
		private HTMLPageRequester htmlRequester;
		private VideoAPIRequester apiRequester;
		private final boolean crawlHtml;
		private final boolean crawlStats;
		private final boolean crawlApi;

		public MultiRequester(String appName, String devKey, boolean crawlHtml,
				boolean crawlStats, boolean crawlApi) {
			this.htmlRequester = new HTMLPageRequester(devKey);
			this.apiRequester = new VideoAPIRequester(appName, devKey);
			this.crawlHtml = crawlHtml;
			this.crawlStats = crawlStats;
			this.crawlApi = crawlApi;
		}
		
		@Override
		public List<Tuple<String, byte[]>> performRequest(String id) 
				throws Exception {

			ArrayList<Tuple<String, byte[]>> returnVal = new ArrayList<>();
			if (this.crawlHtml) {
                LOG.info("Collecting video page for id " + id);
				String videoUrl = "http://www.youtube.com/watch?v=" + id +
						"&gl=US&hl=en";
				byte[] vidHtml = this.htmlRequester.performRequest(videoUrl);
				returnVal.add(new Tuple<String, byte[]>(id + "-content.html", 
						vidHtml));
			}

			if (this.crawlStats) {
                LOG.info("Collecting stats page for id " + id);
				String statsUrl = "http://www.youtube.com/insight_ajax?" +
						"action_get_statistics_and_data=1&v="+ id +
						"&gl=US&hl=en";
				byte[] statsHtml = this.htmlRequester.performRequest(statsUrl);
				returnVal.add(new Tuple<String, byte[]>(id + "-stats.html", 
						statsHtml));
			}
			
			if (this.crawlApi) {
                LOG.info("Collecting api page for id " + id);
				byte[] apiJson = this.apiRequester.performRequest(id);
				returnVal.add(new Tuple<String, byte[]>(id + "-api.json", 
						apiJson));
			}
			
			return returnVal;
		}
	}
}
