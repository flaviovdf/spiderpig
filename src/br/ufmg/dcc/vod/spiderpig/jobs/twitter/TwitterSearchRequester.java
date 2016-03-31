package br.ufmg.dcc.vod.spiderpig.jobs.twitter;

import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.google.common.collect.Sets;

import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableRequester;
import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResultFactory;
import br.ufmg.dcc.vod.spiderpig.jobs.PayloadsFactory;
import br.ufmg.dcc.vod.spiderpig.jobs.QuotaException;
import br.ufmg.dcc.vod.spiderpig.jobs.Request;
import br.ufmg.dcc.vod.spiderpig.jobs.youtube.UnableToCrawlException;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterSearchRequester implements ConfigurableRequester {
	
    private static final int QUOTA_ERROR = 88;
    
    private static final String CONKEY = "worker.job.twitter.conkey";
    private static final String CONSECRET = "worker.job.twitter.consecret";
    private static final String TOKEN = "worker.job.twitter.token";
    private static final String TOKENSECRET = "worker.job.twitter.tokensecret";
    
    private Twitter twitter;
	
    @Override
	public Request createRequest(final CrawlID crawlID) {
    	return new TwitterRequest(crawlID);
	}
    
    @Override
    public Set<String> getRequiredParameters() {
        return Sets.newHashSet(CONKEY, CONSECRET, TOKEN, TOKENSECRET);
    }

    @Override
    public void configurate(Configuration configuration, 
            ConfigurableBuilder configurableBuilder) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        
        cb.setOAuthConsumerKey(configuration.getString(CONKEY));
        cb.setOAuthConsumerSecret(configuration.getString(CONSECRET));
        cb.setOAuthAccessToken(configuration.getString(TOKEN));
        cb.setOAuthAccessTokenSecret(configuration.getString(TOKENSECRET));
        
        this.twitter = new TwitterFactory(cb.build()).getInstance();
    }
    
	private class TwitterRequest implements Request {
		private CrawlID crawlID;
		private int i;
		private Query query;

		public TwitterRequest(CrawlID crawlID) {
			String id = crawlID.getId();
			this.crawlID = crawlID;
			this.i = 0;
			this.query = new Query(id);
		}
		
		@Override
		public CrawlResult continueRequest() throws QuotaException {
			CrawlResultFactory crawlResult = new CrawlResultFactory(crawlID);
	        this.query.setCount(100);
	        StringBuilder returnValue = new StringBuilder();
	            
	        try {
	            QueryResult result;
	            do {
	                result = TwitterSearchRequester.this.twitter.search(query);
	                List<Status> tweets = result.getTweets();
	                for (Status tweet : tweets) {
	                    returnValue.append(this.i++);
	                    returnValue.append(" - ");
	                    returnValue.append(tweet);
	                    returnValue.append(System.lineSeparator());
	                }
	            } while ((this.query = result.nextQuery()) != null);
	        } catch (TwitterException e) {
	            int errorCode = e.getErrorCode();
	            if (errorCode == QUOTA_ERROR) {
	                throw new QuotaException(e);
	            } else {
	                UnableToCrawlException cause = 
	                		new UnableToCrawlException(e);
	                return crawlResult.buildNonQuotaError(cause);
	            }
	        }
	        
	        PayloadsFactory payloadBuilder = new PayloadsFactory();
	        payloadBuilder.addPayload(this.crawlID,
	        		returnValue.toString().getBytes());
	        return crawlResult.buildOK(payloadBuilder.build(), null);
		}
	}
}
