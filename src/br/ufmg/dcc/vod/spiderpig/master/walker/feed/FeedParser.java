package br.ufmg.dcc.vod.spiderpig.master.walker.feed;

import java.util.List;

import br.ufmg.dcc.vod.spiderpig.jobs.Requester;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public interface FeedParser extends Requester<List<CrawlID>> {
}
