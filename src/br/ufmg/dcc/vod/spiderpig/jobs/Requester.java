package br.ufmg.dcc.vod.spiderpig.jobs;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public interface Requester {

	CrawlResult performRequest(CrawlID crawlID) throws QuotaException;

}
