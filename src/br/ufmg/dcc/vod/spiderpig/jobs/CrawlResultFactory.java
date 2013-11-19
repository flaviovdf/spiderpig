package br.ufmg.dcc.vod.spiderpig.jobs;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import br.ufmg.dcc.vod.spiderpig.jobs.youtube.UnableToCrawlException;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult.Builder;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.Payload;

public class CrawlResultFactory {

	public static final List<CrawlID> ELIST_CRAWLIDS = Collections.emptyList();
	public static final List<Payload> ELIST_PAYLOAD = Collections.emptyList();
	public static final Map<String, byte[]> EMAP = Collections.emptyMap();
	private final CrawlID crawlID;
	
	public CrawlResultFactory(CrawlID crawlID) {
		this.crawlID = crawlID;
	}

	public CrawlResult buildOK(Collection<Payload> payloads, 
			List<CrawlID> toQueue) {
		
		Builder resultProtocolBuilder = CrawlResult.newBuilder();
		resultProtocolBuilder.setId(crawlID);
		resultProtocolBuilder.setIsError(false);
				
		if (toQueue != null)
			resultProtocolBuilder.addAllToQueue(toQueue);
		else
			resultProtocolBuilder.addAllToQueue(ELIST_CRAWLIDS);
		
		if (payloads != null)
			resultProtocolBuilder.addAllPayLoad(payloads);
		else
			resultProtocolBuilder.addAllPayLoad(ELIST_PAYLOAD);
				
		return resultProtocolBuilder.build();
	}

	public CrawlResult buildOK() {
		return buildOK(ELIST_PAYLOAD, ELIST_CRAWLIDS);
	}
	
	public CrawlResult buildNonQuotaError(UnableToCrawlException cause) {
		CrawlResult result =
				CrawlResult.newBuilder().setId(crawlID)
				.setIsError(true)
				.setErrorMessage(cause.getCause().getMessage()).build();
		return result;
	}
}