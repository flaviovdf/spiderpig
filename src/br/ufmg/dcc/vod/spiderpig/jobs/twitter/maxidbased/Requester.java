package br.ufmg.dcc.vod.spiderpig.jobs.twitter.maxidbased;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

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

public class Requester implements ConfigurableRequester {

	private static final Logger LOG = Logger.getLogger(Requester.class);
    private static final int QUOTA_ERROR = 429;
    
    private static final String CONKEY = "worker.job.twitter.conkey";
    private static final String CONSECRET = "worker.job.twitter.consecret";
    private static final String TOKEN = "worker.job.twitter.token";
    private static final String TOKENSECRET = "worker.job.twitter.tokensecret";
    
    private Twitter twitter;
    
	@Override
	public Request createRequest(final CrawlID crawlID) {
		return new Request() {
			@Override
			public CrawlResult continueRequest() throws QuotaException {
				return performRequest(crawlID);
			}
		};
	}
    
    public CrawlResult performRequest(CrawlID crawlID) throws QuotaException {
        
        CrawlResultFactory crawlResult = new CrawlResultFactory(crawlID);
        String queryContents = crawlID.getId();
        
        String[] split = queryContents.split("\t");

        long maxId;
        String hashtag;
        if (split.length == 2) {
	        String max = split[0];
	        hashtag = split[1].trim();
	        
	        try {
	        	maxId = Integer.parseInt(max);
	        } catch (NumberFormatException nfme) {
	        	maxId = Long.MAX_VALUE;
	        }
        } else {
        	hashtag = queryContents.trim();
        	maxId = Long.MAX_VALUE;
        }
        
        
        StringBuilder returnValue = new StringBuilder();
        List<CrawlID> toQueue = new ArrayList<>();
        int i = 0;
        
        try {
        	Query query = new Query(hashtag);
            query.setResultType(Query.RECENT);
            if (maxId != Long.MAX_VALUE)
            	query.setMaxId(maxId);
            query.setCount(100);
            
            QueryResult result;
            long minSinceId = Long.MAX_VALUE;
            
            do {
            	LOG.info("Query " + query);
                result = twitter.search(query);
                LOG.info(result.toString());
                minSinceId = Math.min(minSinceId, result.getSinceId());
                
                List<Status> tweets = result.getTweets();
                for (Status tweet : tweets) {
                    returnValue.append(i++);
                    returnValue.append("\t");
                    returnValue.append(hashtag);
                    returnValue.append("\t");
                    returnValue.append(tweet);
                    returnValue.append(System.lineSeparator());
                }
            } while ((query = result.nextQuery()) != null);
            LOG.info("Next time will begin at " + minSinceId);
            toQueue.add(CrawlID.newBuilder().setId(minSinceId+"").build());
        } catch (TwitterException e) {
        	LOG.info(e);
            int errorCode = e.getErrorCode();
            if (errorCode == QUOTA_ERROR) {
                throw new QuotaException(e);
            } else {
                UnableToCrawlException cause = new UnableToCrawlException(e);
                return crawlResult.buildNonQuotaError(cause);
            }
        }
        
        PayloadsFactory payloadBuilder = new PayloadsFactory();
        payloadBuilder.addPayload(crawlID, returnValue.toString().getBytes());
        return crawlResult.buildOK(payloadBuilder.build(), toQueue);
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
}