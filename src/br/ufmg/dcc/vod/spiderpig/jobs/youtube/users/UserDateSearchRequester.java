package br.ufmg.dcc.vod.spiderpig.jobs.youtube.users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
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

public class UserDateSearchRequester implements ConfigurableRequester {

    private static final int _403_QUOTA_ERR = 403;
    
    private YouTube youtube;
    private String apiKey;

    public CrawlResult performRequest(CrawlID crawlID) 
    		throws QuotaException {
        String dates = crawlID.getId();
        String[] split = dates.split("\\s");
        
        String afterDate = split[0];
        String beforeDate = split[1];
        
        CrawlResultFactory crawlResultBuilder = new CrawlResultFactory(crawlID);
        
        try {
            YouTube.Search.List search = youtube.search().list("id");
            search.setKey(this.apiKey);
            search.setPublishedAfter(DateTime.parseRfc3339(afterDate));
            search.setPublishedBefore(DateTime.parseRfc3339(beforeDate));
            search.setType("channel");
            search.setSafeSearch("none");
            search.setOrder("date");
            search.setMaxResults(50l);
            
            SearchListResponse response = search.execute();
        
            StringBuilder channelIdsBuffer = new StringBuilder();
            List<String> channelIds = new ArrayList<>();
            
            String nextPageToken;
            int numResults = 0;
            do {
                List<SearchResult> items = response.getItems();
                for (SearchResult searchResult : items) {
                    ResourceId rId = searchResult.getId();
                    if (rId != null) {
                        String channelId = (String) rId.get("channelId");
                        
                        channelIds.add(channelId);
                        channelIdsBuffer.append(channelId);
                        channelIdsBuffer.append(System.lineSeparator());
                        ++numResults;
                    }
                }
                
                nextPageToken = response.getNextPageToken();
                if (nextPageToken != null) {
                    search.setPageToken(nextPageToken);
                    response = search.execute();
                }
            } while (nextPageToken != null);
            
            channelIdsBuffer.append(numResults);
            byte[] payload = channelIdsBuffer.toString().getBytes();
            
            PayloadsFactory payloadBuilder = new PayloadsFactory();
            payloadBuilder.addPayload(dates, payload);
            return crawlResultBuilder.buildOK(payloadBuilder.build(), null);
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError details = e.getDetails();
            
            if (details != null) {
                Object statusCode = details.get("code");
                
                if (statusCode.equals(_403_QUOTA_ERR)) {
                    QuotaException quotaException = new QuotaException(e);
                    throw quotaException;
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
    
	@Override
	public Request createRequest(final CrawlID crawlID) {
		return new Request() {
			@Override
			public CrawlResult continueRequest() throws QuotaException {
				return performRequest(crawlID);
			}
		};
	}
}