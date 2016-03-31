package br.ufmg.dcc.vod.spiderpig.jobs.youtube.video;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.google.common.collect.Sets;

import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableRequester;
import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResultFactory;
import br.ufmg.dcc.vod.spiderpig.jobs.QuotaException;
import br.ufmg.dcc.vod.spiderpig.jobs.Request;
import br.ufmg.dcc.vod.spiderpig.jobs.youtube.video.api.VideoAPIRequester;
import br.ufmg.dcc.vod.spiderpig.jobs.youtube.video.html.VideoHTMLRequester;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.Payload;

public class VideoRequester implements ConfigurableRequester {

    private static final String CRAWL_HTML = "worker.job.youtube.video.html";
    private static final String CRAWL_API = "worker.job.youtube.video.api";
    
    private boolean crawlHtml;
    private boolean crawlApi;
    
    private VideoAPIRequester apiRequester;
    private VideoHTMLRequester htmlRequester;
    
    @Override
    public void configurate(Configuration configuration, 
            ConfigurableBuilder builder) throws BuildException {
        
        this.apiRequester = 
                builder.build(VideoAPIRequester.class, configuration);
        this.htmlRequester = 
                builder.build(VideoHTMLRequester.class, configuration);
        
        this.crawlHtml = configuration.getBoolean(CRAWL_HTML);
        this.crawlApi = configuration.getBoolean(CRAWL_API);
        
        boolean hasOne = crawlHtml || crawlApi;
        if (!hasOne)
            throw new BuildException("Please set at least one option"
                    + " to crawl", null);
    }

    @Override
    public Set<String> getRequiredParameters() {
        Set<String> req = Sets.newHashSet(CRAWL_HTML, CRAWL_API);
        return req;
    }

	@Override
	public Request createRequest(final CrawlID crawlID) {
		return new Request() {
			@Override
			public CrawlResult continueRequest() throws QuotaException {
		        CrawlResult apiResult = apiRequester.performRequest(crawlID);
		        if (apiResult.hasIsError()) {
		            return apiResult;
		        }
		        
		        CrawlResult htmlResult = htmlRequester.performRequest(crawlID);
		        if (htmlResult.hasIsError()) {
		            return htmlResult;
		        }
		        
		        CrawlResultFactory resultBuilder = 
		        		new CrawlResultFactory(crawlID);
		        List<Payload> payloads = new ArrayList<>();
		        payloads.addAll(apiResult.getPayLoadList());
		        payloads.addAll(htmlResult.getPayLoadList());
		        
		        return resultBuilder.buildOK(payloads, null);
			}
		};
	}
}