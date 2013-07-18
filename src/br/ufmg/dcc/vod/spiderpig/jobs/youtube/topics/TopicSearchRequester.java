package br.ufmg.dcc.vod.spiderpig.jobs.youtube.topics;

import java.io.File;
import java.io.IOException;
import java.util.List;

import br.ufmg.dcc.vod.spiderpig.jobs.QuotaException;
import br.ufmg.dcc.vod.spiderpig.jobs.Requester;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

public class TopicSearchRequester implements Requester<byte[]> {

	private static final int _400_ERR = 400;
	private static final int _403_ERR = 403;
	
	private final YouTube youtube;
	private final String apiKey;

	public TopicSearchRequester(YouTube youtube, String apiKey) {
		this.youtube = youtube;
		this.apiKey = apiKey;
	}
	
	@Override
	public byte[] performRequest(String topicId) 
			throws QuotaException, Exception {
		try {
			YouTube.Search.List search = this.youtube.search().list("id");
			search.setKey(this.apiKey);
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