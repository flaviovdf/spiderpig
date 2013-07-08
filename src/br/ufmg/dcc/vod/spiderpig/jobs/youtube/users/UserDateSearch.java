package br.ufmg.dcc.vod.spiderpig.jobs.youtube.users;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableJobExecutor;
import br.ufmg.dcc.vod.spiderpig.jobs.ThroughputManager;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Sets;

public class UserDateSearch extends AbstractConfigurable<Void> 
		implements ConfigurableJobExecutor {

	private static final String BKOFF_TIME = "worker.job.youtube.backofftime";
	private static final String API_KEY = "worker.job.youtube.user.apikey";
	
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private static final NullInitializer INITIALIZER = new NullInitializer();
	
	private ThroughputManager throughputManager;
	private String apiKey;
	private UserDateSearchRequester requester;
	private YouTube youtube;
	
	@Override
	public Void realConfigurate(Configuration configuration) throws Exception {
		long backOffTime = configuration.getLong(BKOFF_TIME);
		this.throughputManager = new ThroughputManager(0, backOffTime);
		this.apiKey = configuration.getString(API_KEY);
		buildYoutubeService();
		this.requester = new UserDateSearchRequester(this.youtube, this.apiKey);
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

	@Override
	public Set<String> getRequiredParameters() {
		return Sets.newHashSet(API_KEY, BKOFF_TIME);
	}

	@Override
	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver) {
		String dates = id.getId();
		
		try {
			byte[] result = 
					this.throughputManager.sleepAndPerform(dates, 
							this.requester);
			List<CrawlID> emptyList = Collections.emptyList();
			saver.save(dates, result);
			interested.crawlDone(id, emptyList);
		} catch (Exception e) {
			interested.crawlError(id, e.toString());
		}
	}
}