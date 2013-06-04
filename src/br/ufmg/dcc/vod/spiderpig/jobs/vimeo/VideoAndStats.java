package br.ufmg.dcc.vod.spiderpig.jobs.vimeo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
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
		private final HttpClient httpClient;
		
		public VimeoRequester(String devKey) {
			this.vidGetter = new URLGetter(
					"Research-Crawler-APIDEVKEY-" + devKey);
			this.httpClient = new DefaultHttpClient();
			this.httpClient.getParams().setParameter(
			        ClientPNames.COOKIE_POLICY, 
			        CookiePolicy.BROWSER_COMPATIBILITY);
			this.jsonGetter = new URLGetter(
					"Research-Crawler-APIDEVKEY-" + devKey);
			this.jsonGetter.setProperty("Accept", "application/json");
			this.jsonGetter.setProperty("X-Request", "JSON");
			this.jsonGetter.setProperty("X-Requested-With", "XMLHttpRequest");
		}

		@Override
		public List<byte[]> performRequest(String crawlID) 
				throws Exception {
			
			String header = "<crawledvideoid = " + crawlID + ">";
			String footer = "</crawledvideoid>";
			
			//page
			URIBuilder builder = new URIBuilder();
			builder.setScheme("http").
					setHost("vimeo.com").
					setPath("/" + crawlID);
			URI videoUri = builder.build();
			HttpGet getMethod = new HttpGet(videoUri);
			byte[] vidHtml = this.vidGetter.getHtml(this.httpClient,
					getMethod, header, footer);
			
			String uploadDayString = getDayUploadDayString(vidHtml);
			
			SimpleDateFormat parseFormat =
					new SimpleDateFormat("yyyy-MM-dd");
			Date uploadDate = parseFormat.parse(uploadDayString);
			
			//Substract one day
			Calendar cal = Calendar.getInstance();
		    cal.setTime(uploadDate);
		    cal.add(Calendar.DAY_OF_MONTH, -1);
		    Date dayBefore = cal.getTime();

		    //Format Dates
		    SimpleDateFormat requestFormat =
					new SimpleDateFormat("yyyy'%2F'MM'%2F'dd");

		    SimpleDateFormat cacheFormat =
					new SimpleDateFormat("yyyyMMdd");
		    
		    Date now = new Date();
			String today = requestFormat.format(now);
		    String beforeUpload = requestFormat.format(dayBefore);
			
		    String cacheKey = "clip" + cacheFormat.format(dayBefore) +
		    		cacheFormat.format(now);
		    
		    //Referrers
		    String cookie = "stats_end_date=" + today + "; stats_start_date=" +
		    		beforeUpload + "; auto_load_stats=1";
		    
		    builder = new URIBuilder();
		    		builder.setScheme("http").
		    		setHost("vimeo.com").
		    		setPath("/" + crawlID).
		    		setParameter("action", "stats_totals");
		    		
		    URI referrerUri = builder.build();
		    getMethod = new HttpGet(referrerUri);
		    getMethod.setHeader("Cookie", cookie);
		    byte[] referrerHtml = this.jsonGetter.getHtml(this.httpClient,
		    		getMethod, header, footer);
		    
			//stats
			builder = new URIBuilder();
			builder.setScheme("http").
					setHost("vimeo.com").
					setPath("/stats").
					setParameter("action", "clip").
					setParameter("start_date", beforeUpload).
					setParameter("end_date", today).
					setParameter("update_chart", "false").
					setParameter("clip_id", crawlID).
					setParameter("cache_key", cacheKey);
			URI statsUri = builder.build();
			
			getMethod = new HttpGet(statsUri);
			getMethod.setHeader("Cookie", cookie);
			byte[] statsHtml = this.jsonGetter.getHtml(this.httpClient,
					getMethod, header, footer);
			
			return Arrays.asList(vidHtml, statsHtml, referrerHtml); 
		}

		private String getDayUploadDayString(byte[] vidHtml) throws IOException {
			BufferedReader bis = 
					new BufferedReader(new InputStreamReader(
							new ByteArrayInputStream(vidHtml)));
			String inputLine;
			while ((inputLine = bis.readLine()) != null) {
				if (inputLine.contains("uploadDate")) {
					String[] split = inputLine.split("\"");
					return split[3].split("T")[0];
				}
			}
			
			return "";
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
	
	public static void main(String[] args) throws Exception {
		VimeoRequester vimeoRequester = new VimeoRequester("");
		List<byte[]> performRequest = vimeoRequester.performRequest("49345573");
		
		byte[] ba = performRequest.get(2);
		System.out.println(new String(ba));
		
		ba = performRequest.get(1);
		System.out.println(new String(ba));
	}
}
