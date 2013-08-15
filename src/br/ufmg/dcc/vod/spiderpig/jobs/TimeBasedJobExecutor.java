package br.ufmg.dcc.vod.spiderpig.jobs;

import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.Configurable;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResult.ResultState;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.common.collect.Sets;

public class TimeBasedJobExecutor 
		implements Configurable<JobExecutor>, JobExecutor {

	public static final String BKOFF_TIME = "worker.job.backofftime";
	public static final String SLEEP_TIME = "worker.job.sleeptime";
	
	private final Requester requester;
	private ThroughputManager manager;

	public TimeBasedJobExecutor(Requester requester) {
		this.requester = requester;
	}
	
	@Override
	public void crawl(CrawlID id, WorkerInterested interested, 
			FileSaver saver) {
		CrawlResult result = manager.sleepAndPerform(id, requester);
		ResultState state = result.getState();
		switch (state) {
		case OK:
			okResult(id, interested, saver, result);
			break;
		default:
			interested.crawlError(id, result.getErrorCause().toString());
			break;
		}
	}

	private void okResult(CrawlID id, WorkerInterested interested, 
			FileSaver saver, CrawlResult result) {
		
		for (Entry<String, byte[]> e : result.getFilesToSave().entrySet()) {
			String fileID = e.getKey();
			byte[] payload = e.getValue();
			saver.save(fileID, payload);
		}
		interested.crawlDone(id, result.getToQueue());
	}

	@Override
	public JobExecutor configurate(Configuration configuration)
			throws Exception {
		long timeBetweenRequests = 
				configuration.getLong(SLEEP_TIME);
		long backOffTime = configuration.getLong(BKOFF_TIME);
		
		this.manager = new ThroughputManager(timeBetweenRequests,
				backOffTime);
		return this;
	}

	@Override
	public Set<String> getRequiredParameters() {
		return Sets.newHashSet(SLEEP_TIME, BKOFF_TIME);
	}
}