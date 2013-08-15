package br.ufmg.dcc.vod.spiderpig.jobs;

import java.util.List;
import java.util.Map;

import br.ufmg.dcc.vod.spiderpig.jobs.youtube.UnableToCrawlException;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public class CrawlResult {

	public enum ResultState {OK, ERROR}
	
	private final CrawlID crawlID;
	private Map<String, byte[]> filesToSave;
	private List<CrawlID> toQueue;
	private UnableToCrawlException errorCause;
	private ResultState state;
	
	protected CrawlResult(CrawlID crawlID) {
		this.crawlID = crawlID;
	}

	protected void setState(ResultState state) {
		this.state = state;
	}	
	
	protected void setFilesToSave(Map<String, byte[]> filesToSave) {
		this.filesToSave = filesToSave;
	}
	
	protected void setErrorCause(UnableToCrawlException errorCause) {
		this.errorCause = errorCause;
	}
	
	protected void setToQueue(List<CrawlID> toQueue) {
		this.toQueue = toQueue;
	}
	
	public ResultState getState() {
		return state;
	}
	
	public Map<String, byte[]> getFilesToSave() {
		return filesToSave;
	}
	
	public Exception getErrorCause() {
		return errorCause;
	}
	
	public CrawlID getCrawlID() {
		return crawlID;
	}
	
	public List<CrawlID> getToQueue() {
		return toQueue;
	}
	
	public boolean hasAnyError() {
		return !this.state.equals(ResultState.OK);
	}
}
