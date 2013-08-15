package br.ufmg.dcc.vod.spiderpig.jobs;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResult.ResultState;
import br.ufmg.dcc.vod.spiderpig.jobs.youtube.UnableToCrawlException;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public class CrawlResultBuilder {

	public static final List<CrawlID> ELIST = Collections.emptyList();
	public static final Map<String, byte[]> EMAP = Collections.emptyMap();
	private final CrawlID crawlID;
	
	public CrawlResultBuilder(CrawlID crawlID) {
		this.crawlID = crawlID;
	}

	public CrawlResult buildOK(Map<String, byte[]> filesToSave, 
			List<CrawlID> toQueue) {
		CrawlResult result = new CrawlResult(crawlID);
		result.setState(ResultState.OK);
		result.setFilesToSave(filesToSave);
		result.setToQueue(toQueue);
		result.setErrorCause(null);
		return result;
	}

	public CrawlResult buildOK(Map<String, byte[]> filesToSave) {
		return buildOK(filesToSave, ELIST);
	}
	
	public CrawlResult buildOK(List<CrawlID> toQueue) {
		return buildOK(EMAP, toQueue);
	}
	
	public CrawlResult buildOK() {
		return buildOK(EMAP, ELIST);
	}
	
	public CrawlResult buildNonQuotaError(UnableToCrawlException cause) {
		CrawlResult result = new CrawlResult(crawlID);
		result.setState(ResultState.ERROR);
		result.setFilesToSave(null);
		result.setErrorCause(cause);
		return result;
	}
}