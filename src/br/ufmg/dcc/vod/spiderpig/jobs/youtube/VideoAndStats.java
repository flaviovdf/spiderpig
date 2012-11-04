package br.ufmg.dcc.vod.spiderpig.jobs.youtube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.common.Tuple;
import br.ufmg.dcc.vod.spiderpig.common.config.VoidArguments;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableJobExecutor;
import br.ufmg.dcc.vod.spiderpig.jobs.Requester;
import br.ufmg.dcc.vod.spiderpig.jobs.ThroughputManager;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public class VideoAndStats implements ConfigurableJobExecutor {

	private static final Logger LOG = Logger.getLogger(VideoAndStats.class);
	
	private static final String NL = System.lineSeparator();
	private static final String SLEEP_TIME = "worker.job.youtube.sleeptime";
	private static final String DEV_KEY = "worker.job.youtube.devkey";
	
	private ThroughputManager throughputManager;
	private VideoRequester requester;
	
	@Override
	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver) {
		Tuple<String, String> result;
		String sid = id.getId();
		LOG.info("Received id " + id);
		try {
			result = this.throughputManager.sleepAndPerform(sid, requester);
			saver.save(sid + "-content.html", result.first.getBytes());
			saver.save(sid + "-stats.html", result.second.getBytes());
			interested.crawlDone(id, null);
			LOG.info("Sending result for id " + id);
		} catch (Exception e) {
			LOG.error("Error at id " + id, e);
			interested.crawlError(id, e.toString());
		}

	}

	private class VideoRequester implements Requester<Tuple<String, String>> {

		private final String ua;
		public VideoRequester(String devKey) {
			this.ua = "Research-Crawler-APIDEVKEY-" + devKey;
		}

		@Override
		public Tuple<String, String> performRequest(String crawlID) 
				throws Exception {
			
			URL videoUrl = new URL("http://www.youtube.com/watch?v=" + crawlID +
					"&gl=US&hl=en");
			StringBuilder vidHtml = getHtml(videoUrl, crawlID);
			vidHtml.trimToSize();
			
			URL statsUrl = new URL("http://www.youtube.com/insight_ajax?" +
					"action_get_statistics_and_data=1&v="+ crawlID +
					"&gl=US&hl=en");
			StringBuilder statsHtml = getHtml(statsUrl, crawlID);
			statsHtml.trimToSize();
			
			return new Tuple<String, String>(vidHtml.toString(), 
					statsHtml.toString());
		}
		
		private StringBuilder getHtml(URL u, String crawlID) throws Exception {
			BufferedReader in = null;
			try {
				URLConnection openConnection = u.openConnection();
				openConnection.setRequestProperty("User-Agent", ua);
				
				openConnection.connect();
				
				StringBuilder html = new StringBuilder();
				
				html.append("<crawledvideoid = "+ crawlID +" >");
				html.append(NL);
				
				String inputLine;
				in = new BufferedReader(new InputStreamReader(
						openConnection.getInputStream()));
				while ((inputLine = in.readLine()) != null) {
					html.append(inputLine);
					html.append(NL);
				}
				
				html.append("</crawledvideoid>");
				html.append(NL);
				return  html;
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	@Override
	public VoidArguments configurate(Configuration configuration) {
		long timeBetweenRequests = configuration.getLong(SLEEP_TIME);
		String devKey = configuration.getString(DEV_KEY);
		
		this.throughputManager = new ThroughputManager(timeBetweenRequests);
		this.requester = new VideoRequester(devKey);
		
		return null;
	}
}