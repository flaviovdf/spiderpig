package br.ufmg.dcc.vod.spiderpig.jobs.twitter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableJobExecutor;
import br.ufmg.dcc.vod.spiderpig.jobs.ThroughputManager;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public class TwitterSearch extends AbstractConfigurable<Void> 
		implements ConfigurableJobExecutor {

	private static final String BKOFF_TIME = "worker.job.twitter.backofftime";
	private static final String SLEEP_TIME = "worker.job.twitter.sleeptime";
	private static final String CONKEY = "worker.job.twitter.conkey";
	private static final String CONSECRET = "worker.job.twitter.consecret";
	private static final String TOKEN = "worker.job.twitter.token";
	private static final String TOKENSECRET = "worker.job.twitter.tokensecret";
	
	private ThroughputManager throughputManager;
	private TwitterSearchRequester requester;
	
	@Override
	public Set<String> getRequiredParameters() {
		return new HashSet<String>(Arrays.asList(SLEEP_TIME));
	}

	@Override
	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver) {
		try {
			byte[] payload = this.throughputManager.sleepAndPerform(id.getId(), 
					this.requester);
			saver.save(id.getId(), payload);
			List<CrawlID> emptyList = Collections.emptyList();
			interested.crawlDone(id, emptyList);
		} catch (Exception e) {
			interested.crawlError(id, e.getMessage());
		}
	}

	@Override
	public Void realConfigurate(Configuration configuration) throws Exception {
		long timeBetweenRequests = configuration.getLong(SLEEP_TIME);
		long backOffTime = configuration.getLong(BKOFF_TIME);
		this.throughputManager = new ThroughputManager(timeBetweenRequests,
				backOffTime);
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		
		cb.setOAuthConsumerKey(configuration.getString(CONKEY));
		cb.setOAuthConsumerSecret(configuration.getString(CONSECRET));
		cb.setOAuthAccessToken(configuration.getString(TOKEN));
		cb.setOAuthAccessTokenSecret(configuration.getString(TOKENSECRET));
		
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		this.requester = new TwitterSearchRequester(twitter);
		
		return null;
	}
}