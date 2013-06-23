package br.ufmg.dcc.vod.spiderpig.jobs.twitter;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import br.ufmg.dcc.vod.spiderpig.jobs.Requester;

public class TwitterSearchRequester implements Requester<byte[]> {

	private final Twitter twitter;

	public TwitterSearchRequester(Twitter twitter) {
		this.twitter = twitter;
	}
	
	@Override
	public byte[] performRequest(String crawlID) throws Exception {
		Query query = new Query(crawlID);
		query.setCount(100);
		QueryResult result;
		StringBuilder returnValue = new StringBuilder();
		do {
			result = twitter.search(query);
			List<Status> tweets = result.getTweets();
			int i = 0;
			for (Status tweet : tweets) {
				returnValue.append(i++);
				returnValue.append(" - ");
				returnValue.append(tweet);
				returnValue.append(System.lineSeparator());
			}
		} while ((query = result.nextQuery()) != null);
		
		return returnValue.toString().getBytes();
	}
}