package br.ufmg.dcc.vod.spiderpig.jobs.test;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResult;
import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResultBuilder;
import br.ufmg.dcc.vod.spiderpig.jobs.PayloadBuilder;
import br.ufmg.dcc.vod.spiderpig.jobs.Requester;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

public class TestJobRequester implements Requester {

	private static final int INT_SIZE = Integer.SIZE / 8;
	private final RandomizedSyncGraph g;

	public TestJobRequester(RandomizedSyncGraph g) {
		this.g = g;
	}

	@Override
	public CrawlResult performRequest(CrawlID crawlID) {
		int vertex = Integer.parseInt(crawlID.getId());
		int[] neighbours = g.getNeighbours(vertex);
		
		ByteBuffer buff = ByteBuffer.allocate(neighbours.length * INT_SIZE);
		List<CrawlID> toQueue = new LinkedList<>();
		for (int n : neighbours) {
			toQueue.add(CrawlID.newBuilder().setId(""+n).build());
			buff.putInt(n);
		}
			
		buff.rewind();
		
		PayloadBuilder payloadBuilder = new PayloadBuilder();
		payloadBuilder.addPayload(crawlID.getId(), buff.array());
		return new CrawlResultBuilder(crawlID).buildOK(payloadBuilder.build(),
				toQueue);
	}
}
