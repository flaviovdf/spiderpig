package br.ufmg.dcc.vod.spiderpig.jobs.youtube.users.subs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.ufmg.dcc.vod.spiderpig.common.Tuple;
import br.ufmg.dcc.vod.spiderpig.jobs.QuotaException;
import br.ufmg.dcc.vod.spiderpig.jobs.Requester;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.common.collect.Sets;

public class UserDataRequester 
		implements Requester<Tuple<Channel, List<Subscription>>> {

	private static final String SUB_DETAILS = "id,snippet,contentDetails," +
			"subscriberSnippet";
	private static final String USER_DETAILS = "id,snippet,brandingSettings," +
			"contentDetails,invideoPromotion,statistics,topicDetails";
	private final Set<String> SUB_ORDERS = 
			Sets.newHashSet("relevance", "unread", "alphabetical");
	
	private static final int _403_QUOTA_ERR = 403;
	
	private final YouTube youtube;
	private final String apiKey;
	
	public UserDataRequester(YouTube youtube, String apiKey) {
		this.youtube = youtube;
		this.apiKey = apiKey;
	}

	@Override
	public Tuple<Channel, List<Subscription>> performRequest(String crawlID)
			throws QuotaException, Exception {
		
		Channel userChannel = getUserChannel(crawlID);
		List<Subscription> links = new ArrayList<>();
		
		for (String order : SUB_ORDERS) {
			links.addAll(getLinks(userChannel.getId(), order));
		}
		
		return new Tuple<Channel, List<Subscription>>(userChannel, links);
	}

	private List<Subscription> getLinks(String userID, String order) 
			throws IOException {
		YouTube.Subscriptions.List subsList = 
				youtube.subscriptions().list(SUB_DETAILS);
		
		subsList.setKey(this.apiKey);
		subsList.setMaxResults(50l);
		subsList.setId(userID);
		subsList.setOrder(order);
		
		List<Subscription> returnValue = new ArrayList<>();
		String nextPageToken;
		try {
			SubscriptionListResponse response = subsList.execute();
			do {
				List<Subscription> items = response.getItems();
				returnValue.addAll(items);
				
				nextPageToken = response.getNextPageToken();
				if (nextPageToken != null) {
					subsList.setPageToken(nextPageToken);
					response = subsList.execute();
				}
			} while (nextPageToken != null);
			
		} catch (GoogleJsonResponseException e) {
			GoogleJsonError details = e.getDetails();
			
			if (details != null) {
				Object statusCode = details.get("code");
				
				if (statusCode.equals(_403_QUOTA_ERR)) {
					throw new QuotaException(e);
				} else {
					throw e;
				}
			} else {
				throw e;
			}
		}
		
		return returnValue;
	}

	private Channel getUserChannel(String userID)
			throws IOException, QuotaException, GoogleJsonResponseException {
		
		YouTube.Channels.List userList = youtube.channels().list(USER_DETAILS);
		userList.setKey(this.apiKey);
		userList.setMaxResults(50l);
		userList.setId(userID);
		
		try {
			ChannelListResponse response = userList.execute();
				List<Channel> items = response.getItems();
				if (items.isEmpty()) {
					throw new IOException("User not found " + userID);
				} else {
					return items.get(0);
				}
		} catch (GoogleJsonResponseException e) {
			GoogleJsonError details = e.getDetails();
			
			if (details != null) {
				Object statusCode = details.get("code");
				
				if (statusCode.equals(_403_QUOTA_ERR)) {
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