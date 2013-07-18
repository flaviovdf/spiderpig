package br.ufmg.dcc.vod.spiderpig.jobs.youtube.topics;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableJobExecutor;
import br.ufmg.dcc.vod.spiderpig.jobs.ThroughputManager;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.jobs.youtube.YTConstants;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Sets;

public class TopicSearch extends AbstractConfigurable<Void> 
		implements ConfigurableJobExecutor {

	private String apiKey;
	private YouTube youtube;
	private ThroughputManager throughputManager;
	private TopicSearchRequester requester;
	
	@Override
	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver) {
		String topicIdFreebaseFmt = id.getId();
		String[] split = topicIdFreebaseFmt.split("\\.");
		String topicName = split[split.length - 1];
		String topicId = "/m/" + topicName;
		
		try {
			byte[] result = 
					this.throughputManager.sleepAndPerform(topicId, requester);
			List<CrawlID> emptyList = Collections.emptyList();
			saver.save(topicName, result);
			interested.crawlDone(id, emptyList);
		} catch (Exception e) {
			interested.crawlError(id, e.toString());
		}
	}

	@Override
	public Set<String> getRequiredParameters() {
		return Sets.newHashSet(YTConstants.API_KEY, 
				YTConstants.BKOFF_TIME);
	}
	
	@Override
	public Void realConfigurate(Configuration configuration) throws Exception {
		long backOffTime = configuration.getLong(YTConstants.BKOFF_TIME);
		this.throughputManager = new ThroughputManager(0, backOffTime);
		this.apiKey = configuration.getString(YTConstants.API_KEY);
		this.youtube = YTConstants.buildYoutubeService();
		this.requester = new TopicSearchRequester(this.youtube, this.apiKey);
		return null;
	}
}