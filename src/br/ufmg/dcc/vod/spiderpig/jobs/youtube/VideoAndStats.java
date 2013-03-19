package br.ufmg.dcc.vod.spiderpig.jobs.youtube;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.common.Tuple;
import br.ufmg.dcc.vod.spiderpig.common.URLGetter;
import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableJobExecutor;
import br.ufmg.dcc.vod.spiderpig.jobs.Requester;
import br.ufmg.dcc.vod.spiderpig.jobs.ThroughputManager;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public class VideoAndStats extends AbstractConfigurable<Void> 
	implements ConfigurableJobExecutor {

	private static final Logger LOG = Logger.getLogger(VideoAndStats.class);
	
	private static final String SLEEP_TIME = "worker.job.youtube.sleeptime";
	private static final String DEV_KEY = "worker.job.youtube.devkey";
	
	private ThroughputManager throughputManager;
	private VideoRequester requester;
	
	@Override
	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver) {
		Tuple<byte[], byte[]> result;
		String sid = id.getId();
		LOG.info("Received id " + id);
		try {
			result = this.throughputManager.sleepAndPerform(sid, requester);
			saver.save(sid + "-content.html", result.first);
			saver.save(sid + "-stats.html", result.second);
			interested.crawlDone(id, null);
			LOG.info("Sending result for id " + id);
		} catch (Exception e) {
			LOG.error("Error at id " + id, e);
			interested.crawlError(id, e.toString());
		}
	}

	private static class VideoRequester 
			implements Requester<Tuple<byte[], byte[]>> {

		private final URLGetter getter;
		
		public VideoRequester(String devKey) {
			this.getter = new URLGetter();
			this.getter.setProperty("User-Agent", 
					"Research-Crawler-APIDEVKEY-" + devKey);
		}

		@Override
		public Tuple<byte[], byte[]> performRequest(String crawlID) 
				throws Exception {
			
			URL videoUrl = new URL("http://www.youtube.com/watch?v=" + crawlID +
					"&gl=US&hl=en");
			String header = "<crawledvideoid = " + crawlID + ">";
			String footer = "</crawledvideoid>";
			byte[] vidHtml = this.getter.getHtml(videoUrl, header, footer);
			
			URL statsUrl = new URL("http://www.youtube.com/insight_ajax?" +
					"action_get_statistics_and_data=1&v="+ crawlID +
					"&gl=US&hl=en");
			byte[] statsHtml = this.getter.getHtml(statsUrl, header, footer);
			return new Tuple<byte[], byte[]>(vidHtml, statsHtml); 
		}
	}

	@Override
	public Void realConfigurate(Configuration configuration) {
		long timeBetweenRequests = configuration.getLong(SLEEP_TIME);
		String devKey = configuration.getString(DEV_KEY);
		
		this.throughputManager = new ThroughputManager(timeBetweenRequests);
		this.requester = new VideoRequester(devKey);
		
		return null;
	}

	@Override
	public Set<String> getRequiredParameters() {
		return new HashSet<String>(Arrays.asList(SLEEP_TIME, DEV_KEY));
	}
}