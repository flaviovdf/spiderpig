package br.ufmg.dcc.vod.spiderpig.jobs.youtube.topics;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableJobExecutor;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.common.collect.Sets;

public class TopicSearch extends AbstractConfigurable<Void> 
		implements ConfigurableJobExecutor {

	private static final String API_KEY = "worker.job.youtube.topic.apikey";
	
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private static final NullInitializer INITIALIZER = new NullInitializer();
	private static final SimpleDateFormat RFC3339_FMT = 
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private static final int _400_ERR = 400;
	
	private String apiKey;
	private YouTube youtube;
	
	@Override
	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver) {
		String topicId = id.getId();
		try {
			
			Calendar cal = Calendar.getInstance();
			
			cal.add(Calendar.DAY_OF_YEAR, -99);
			String bottomDate = RFC3339_FMT.format(cal.getTime());
			
			cal.add(Calendar.DAY_OF_YEAR, +1);
			String topDate = RFC3339_FMT.format(cal.getTime());
			
			YouTube.Search.List search = youtube.search().list("id,snippet");
			search.setKey(this.apiKey);
			search.setPublishedAfter(DateTime.parseRfc3339(bottomDate));
			search.setPublishedBefore(DateTime.parseRfc3339(topDate));
			search.setTopicId(topicId);
			search.setType("video");
			search.setSafeSearch("none");
			search.setMaxResults(50l);
			
			SearchListResponse response = search.execute();
			String nextPageToken;
			do {
				List<SearchResult> items = response.getItems();
				
				nextPageToken = response.getNextPageToken();
				if (nextPageToken != null) {
					search.setPageToken(nextPageToken);
					response = search.execute();
				}
			} while (nextPageToken != null);
			
			interested.crawlDone(id, toQueue);
		} catch (GoogleJsonResponseException e) {
			GoogleJsonError details = e.getDetails();
			
			//The API returns code 400 if topic was not found.
			if (details != null && details.get("code").equals(_400_ERR)) {
				interested.crawlError(id, "Topic not found");
			} else {
				interested.crawlError(id, e.getMessage());
			}
		} catch (IOException e) {
			interested.crawlError(id, e.getMessage());
		}
	}

	@Override
	public Set<String> getRequiredParameters() {
		return Sets.newHashSet(API_KEY);
	}
	
	@Override
	public Void realConfigurate(Configuration configuration) throws Exception {
		this.apiKey = configuration.getString(API_KEY);
		buildYoutubeService();
		return null;
	}

	private void buildYoutubeService() {
		this.youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, 
				INITIALIZER).setApplicationName("Simple API Access").build();
	}
	
	private static class NullInitializer implements HttpRequestInitializer {
		@Override
		public void initialize(HttpRequest arg0) throws IOException {
		}
	}
	
	public static void main(String[] args) {
		TopicSearch ts = new TopicSearch();
		ts.apiKey = "AIzaSyBv2cM4hW0NU15-LFCgJe-0ILHj8N7_nQ0";
		ts.buildYoutubeService();
		
		String topic = "/m/01yrx";
		CrawlID id = CrawlID.newBuilder().setId(topic).build();
		ts.crawl(id, null, null);
	}
}