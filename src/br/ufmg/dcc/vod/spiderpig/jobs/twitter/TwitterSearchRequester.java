package br.ufmg.dcc.vod.spiderpig.jobs.twitter;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import br.ufmg.dcc.vod.spiderpig.jobs.QuotaException;
import br.ufmg.dcc.vod.spiderpig.jobs.Requester;

public class TwitterSearchRequester implements Requester<byte[]> {

	private static final int QUOTA_ERROR = 429;
	private final Twitter twitter;
	
	public TwitterSearchRequester(Twitter twitter) {
		this.twitter = twitter;
	}
	
	@Override
	public byte[] performRequest(String crawlID) 
			throws QuotaException, TwitterException {
		
		Query query = new Query(crawlID);
		query.setCount(100);
		StringBuilder returnValue = new StringBuilder();
		int i = 0;
			
		try {
			QueryResult result;
			do {
				result = twitter.search(query);
				List<Status> tweets = result.getTweets();
				for (Status tweet : tweets) {
					returnValue.append(i++);
					returnValue.append(" - ");
					returnValue.append(tweet);
					returnValue.append(System.lineSeparator());
				}
			} while ((query = result.nextQuery()) != null);
		} catch (TwitterException e) {
			int errorCode = e.getErrorCode();
			if (errorCode == QUOTA_ERROR) {
				throw new QuotaException(e);
			} else {
				throw e;
			}
		}
		
		return returnValue.toString().getBytes();
	}
}