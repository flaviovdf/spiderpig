package br.ufmg.dcc.vod.spiderpig.jobs;

import java.util.HashMap;
import java.util.Map;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public class PayloadBuilder {

	private Map<String, byte[]> filesToSave;

	public PayloadBuilder() {
		this.filesToSave = new HashMap<String, byte[]>();
	}

	public PayloadBuilder addPayload(CrawlID crawlID, byte[] payload) {
		return addPayload(crawlID, payload, "");
	}
	
	public PayloadBuilder addPayload(CrawlID crawlID, byte[] payload, 
			String suffix) {
		String fileName = crawlID.getResourceType() + "-" + crawlID.getId() + 
				"-" + suffix;
		addPayload(fileName, payload);
		return this;
	}
	
	public PayloadBuilder addPayload(String fileName, byte[] payload) {
		this.filesToSave.put(fileName, payload);
		return this;
	}
	
	public Map<String, byte[]> build() {
		return this.filesToSave;
	}
	
}
