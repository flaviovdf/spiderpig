package br.ufmg.dcc.vod.spiderpig.jobs.youtube.topics;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableJobExecutor;
import br.ufmg.dcc.vod.spiderpig.jobs.QuotaException;
import br.ufmg.dcc.vod.spiderpig.jobs.Requester;
import br.ufmg.dcc.vod.spiderpig.jobs.ThroughputManager;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.common.collect.Sets;

public class TopicSearch extends AbstractConfigurable<Void> 
		implements ConfigurableJobExecutor {

	private static final String BKOFF_TIME = "worker.job.youtube.backofftime";
	private static final String API_KEY = "worker.job.youtube.topic.apikey";
	
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private static final NullInitializer INITIALIZER = new NullInitializer();
//	private static final SimpleDateFormat RFC3339_FMT = 
//			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private static final int _400_ERR = 400;
	private static final int _403_ERR = 403;
	
	private String apiKey;
	private YouTube youtube;
	private ThroughputManager throughputManager;
	private TopicSearchRequester requester;
	
	private class TopicSearchRequester implements Requester<byte[]> {

		@Override
		public byte[] performRequest(String topicId) 
				throws QuotaException, Exception {
			try {
//				Calendar cal = Calendar.getInstance();
//				
//				cal.add(Calendar.DAY_OF_YEAR, -99);
//				String bottomDate = RFC3339_FMT.format(cal.getTime());
//				
//				cal.add(Calendar.DAY_OF_YEAR, +1);
//				String topDate = RFC3339_FMT.format(cal.getTime());
				
				YouTube.Search.List search = youtube.search().list("id");
				search.setKey(TopicSearch.this.apiKey);
//				search.setPublishedAfter(DateTime.parseRfc3339(bottomDate));
//				search.setPublishedBefore(DateTime.parseRfc3339(topDate));
				search.setTopicId(topicId);
				search.setType("video");
				search.setSafeSearch("none");
				search.setMaxResults(50l);
				
				SearchListResponse response = search.execute();
				StringBuilder videoIdsBuffer = new StringBuilder();
				String nextPageToken;
				do {
					List<SearchResult> items = response.getItems();
					
					for (SearchResult searchResult : items) {
						ResourceId rId = searchResult.getId();
						if (rId != null) {
							String videoId = (String) rId.get("videoId");
							videoIdsBuffer.append(videoId);
							videoIdsBuffer.append(File.separatorChar);
						}
					}
					
					nextPageToken = response.getNextPageToken();
					if (nextPageToken != null) {
						search.setPageToken(nextPageToken);
						response = search.execute();
					}
				} while (nextPageToken != null);
				
				return videoIdsBuffer.toString().getBytes();
			} catch (GoogleJsonResponseException e) {
				GoogleJsonError details = e.getDetails();
				
				if (details != null) {
					Object statusCode = details.get("code");
					
					if (statusCode.equals(_400_ERR)) {
						//The API returns code 400 if topic was not found.
						throw new IOException("Topic not found", e);
					} else if (statusCode.equals(_403_ERR)) {
						//The API return code 403 in case quota has exceeded
						throw new QuotaException(e);
					} else {
						throw e;
					}
				} else {
					throw e;
				}
			}
		}
	}
	
	@Override
	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver) {
		String topicIdFreebaseFmt = id.getId();
		String[] split = topicIdFreebaseFmt.split("\\.");
		String topicName = split[split.length - 1];
		String topicId = "/m/" + topicName;
		
		try {
			byte[] result = 
					this.throughputManager.sleepAndPerform(topicId, requester);
			List<CrawlID> emptyList = Collections.emptyList();
			saver.save(topicName, result);
			interested.crawlDone(id, emptyList);
		} catch (Exception e) {
			interested.crawlError(id, e.toString());
		}
	}

	@Override
	public Set<String> getRequiredParameters() {
		return Sets.newHashSet(API_KEY, BKOFF_TIME);
	}
	
	@Override
	public Void realConfigurate(Configuration configuration) throws Exception {
		long backOffTime = configuration.getLong(BKOFF_TIME);
		this.throughputManager = new ThroughputManager(0, backOffTime);
		this.apiKey = configuration.getString(API_KEY);
		buildYoutubeService();
		this.requester = new TopicSearchRequester();
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
}