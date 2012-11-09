package br.ufmg.dcc.vod.spiderpig.jobs.vimeo;

import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

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
	
	private static final String SLEEP_TIME = "worker.job.vimeo.sleeptime";
	private static final String DEV_KEY = "worker.job.youtube.devkey";
	
	private ThroughputManager throughputManager;
	private VimeoRequester requester;
	
	@Override
	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver) {
		String sid = id.getId();
		LOG.info("Received id " + id);
		try {
			List<byte[]> result = 
					this.throughputManager.sleepAndPerform(sid, requester);
			saver.save(sid + "-content.html", result.get(0));
			saver.save(sid + "-stats.html", result.get(1));
			saver.save(sid + "-referer.html", result.get(2));
			interested.crawlDone(id, null);
			LOG.info("Sending result for id " + id);
		} catch (Exception e) {
			LOG.error("Error at id " + id, e);
			interested.crawlError(id, e.toString());
		}
	}

	private static class VimeoRequester implements Requester<List<byte[]>> {

		private final URLGetter vidGetter;
		private final URLGetter jsonGetter;
		
		public VimeoRequester(String devKey) {
			this.vidGetter = new URLGetter();
			this.vidGetter.setProperty("User-Agent", 
					"Research-Crawler-APIDEVKEY-" + devKey);
			
			this.jsonGetter = new URLGetter();
			this.jsonGetter.setProperty("Accept", "application/json");
			this.jsonGetter.setProperty("X-Request", "JSON");
			this.jsonGetter.setProperty("X-Requested-With", "XMLHttpRequest");
		}

		@Override
		public List<byte[]> performRequest(String crawlID) 
				throws Exception {
			
			String header = "<crawledvideoid = " + crawlID + ">";
			String footer = "</crawledvideoid>";
			
			URL videoUrl = new URL("http://vimeo.com/" + crawlID);
			byte[] vidHtml = this.vidGetter.getHtml(videoUrl, header, footer);
			
			int year = Calendar.getInstance().get(Calendar.YEAR);
			int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
			int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK_IN_MONTH);
			
			String today = year +  "%2F" + month + "%2F" + day;
			
			URL statsUrl = new URL("http://vimeo.com/stats?action=clip" +
					"&start_date=2000%2F01%2F01" +
					"&end_date=" + today +
					"&clip_id=" + crawlID);
			byte[] statsHtml = this.jsonGetter.getHtml(statsUrl, header, footer);
			
			URL referrerUrl = new URL("http://vimeo.com/" + crawlID +
					"?action=stats_totals");
			byte[] referrerHtml = this.jsonGetter.getHtml(referrerUrl, header, 
					footer);
			
			return Arrays.asList(vidHtml, statsHtml, referrerHtml); 
		}
	}
	
	@Override
	public Void realConfigurate(Configuration configuration) {
		long timeBetweenRequests = configuration.getLong(SLEEP_TIME);
		String devKey = configuration.getString(DEV_KEY);
		
		this.throughputManager = new ThroughputManager(timeBetweenRequests);
		this.requester = new VimeoRequester(devKey);
		
		return null;
	}

	@Override
	public Set<String> getRequiredParameters() {
		return new HashSet<String>(Arrays.asList(SLEEP_TIME, DEV_KEY));
	}
}
