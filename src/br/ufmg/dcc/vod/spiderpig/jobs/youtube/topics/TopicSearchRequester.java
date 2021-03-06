package br.ufmg.dcc.vod.spiderpig.jobs.youtube.topics;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.common.collect.Sets;

import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableRequester;
import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResultFactory;
import br.ufmg.dcc.vod.spiderpig.jobs.PayloadsFactory;
import br.ufmg.dcc.vod.spiderpig.jobs.QuotaException;
import br.ufmg.dcc.vod.spiderpig.jobs.Request;
import br.ufmg.dcc.vod.spiderpig.jobs.youtube.UnableToCrawlException;
import br.ufmg.dcc.vod.spiderpig.jobs.youtube.YTConstants;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;

public class TopicSearchRequester implements ConfigurableRequester {

    private static final int _400_ERR = 400;
    private static final int _403_ERR = 403;
    
    private YouTube youtube;
    private String apiKey;

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
        
        String topicIdFreebaseFmt = crawlID.getId();
        String[] split = topicIdFreebaseFmt.split("\\.");
        String topicName = split[split.length - 1];
        String topicId = "/m/" + topicName;
        
        CrawlResultFactory crawlResultBuilder = new CrawlResultFactory(crawlID);
        PayloadsFactory payloadBuilder = new PayloadsFactory();
        
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
            
            payloadBuilder.addPayload(topicName, 
                    videoIdsBuffer.toString().getBytes());
            return crawlResultBuilder.buildOK(payloadBuilder.build(), null);
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError details = e.getDetails();
            
            if (details != null) {
                Object statusCode = details.get("code");
                
                if (statusCode.equals(_400_ERR)) {
                    //The API returns code 400 if topic was not found.
                    return crawlResultBuilder.buildNonQuotaError(
                            new UnableToCrawlException(
                                    new IOException("Topic not found", e)));
                } else if (statusCode.equals(_403_ERR)) {
                    //The API return code 403 in case quota has exceeded
                    throw new QuotaException(e);
                } else {
                    return crawlResultBuilder.buildNonQuotaError(
                            new UnableToCrawlException(e));
                }
            } else {
                return crawlResultBuilder.buildNonQuotaError(
                        new UnableToCrawlException(e));
            }
        } catch (IOException e) {
             return crawlResultBuilder.buildNonQuotaError(
                     new UnableToCrawlException(e));
        }
    }

    @Override
    public Set<String> getRequiredParameters() {
        return Sets.newHashSet(YTConstants.API_KEY);
    }

    @Override
    public void configurate(Configuration configuration, 
            ConfigurableBuilder builder) throws BuildException {
        this.youtube = YTConstants.buildYoutubeService();
        this.apiKey = configuration.getString(YTConstants.API_KEY);
    }
}