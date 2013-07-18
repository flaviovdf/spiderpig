package br.ufmg.dcc.vod.spiderpig.jobs.youtube.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.Tuple;
import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableJobExecutor;
import br.ufmg.dcc.vod.spiderpig.jobs.ThroughputManager;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.jobs.youtube.YTConstants;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Sets;

public class UserDateSearch extends AbstractConfigurable<Void> 
		implements ConfigurableJobExecutor {

	private ThroughputManager throughputManager;
	private String apiKey;
	private UserDateSearchRequester requester;
	private YouTube youtube;
	
	@Override
	public Void realConfigurate(Configuration configuration) throws Exception {
		long backOffTime = configuration.getLong(YTConstants.BKOFF_TIME);
		this.throughputManager = new ThroughputManager(0, backOffTime);
		this.apiKey = configuration.getString(YTConstants.API_KEY);
		this.youtube = YTConstants.buildYoutubeService();
		this.requester = new UserDateSearchRequester(this.youtube, this.apiKey);
		return null;
	}

	@Override
	public Set<String> getRequiredParameters() {
		return Sets.newHashSet(YTConstants.BKOFF_TIME, 
				YTConstants.API_KEY);
	}

	@Override
	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver) {
		String dates = id.getId();
		
		try {
			Tuple<List<String>, byte[]> result = 
					this.throughputManager.sleepAndPerform(dates, 
							this.requester);
			saver.save(dates, result.second);
			
			List<CrawlID> toCrawl = new ArrayList<>();
			for (String channelId : result.first) {
				toCrawl.add(CrawlID.newBuilder().setId(channelId).build());
			}
			
			interested.crawlDone(id, toCrawl);
		} catch (Exception e) {
			interested.crawlError(id, e.toString());
		}
	}
}