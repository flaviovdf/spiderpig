package br.ufmg.dcc.vod.spiderpig.jobs.test;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableRequester;
import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResultFactory;
import br.ufmg.dcc.vod.spiderpig.jobs.PayloadsFactory;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;

import com.google.common.collect.Sets;

public class TestJobRequester implements ConfigurableRequester {

	private static final int INT_SIZE = Integer.SIZE / 8;
	public static final String GRAPH = "G";
	
	private RandomizedSyncGraph g;

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
		
		PayloadsFactory payloadBuilder = new PayloadsFactory();
		payloadBuilder.addPayload(crawlID.getId(), buff.array());
		return new CrawlResultFactory(crawlID).buildOK(payloadBuilder.build(),
				toQueue);
	}

	@Override
	public void configurate(Configuration configuration,
			ConfigurableBuilder builder) throws BuildException {
		this.g = (RandomizedSyncGraph) configuration.getProperty(GRAPH);
		
	}

	@Override
	public Set<String> getRequiredParameters() {
		return Sets.newHashSet(GRAPH);
	}
}
