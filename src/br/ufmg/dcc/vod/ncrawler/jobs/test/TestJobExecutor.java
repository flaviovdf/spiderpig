package br.ufmg.dcc.vod.ncrawler.jobs.test;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.jobs.WorkerInterested;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Ids.CrawlID;

public class TestJobExecutor implements JobExecutor {

	private static final int INT_SIZE = Integer.SIZE / 8;
	private final RandomizedSyncGraph g;

	public TestJobExecutor(RandomizedSyncGraph g) {
		this.g = g;
	}

	@Override
	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver) {
		int vertex = Integer.parseInt(id.getId());
		int[] neighbours = g.getNeighbours(vertex);
		
		ByteBuffer buff = ByteBuffer.allocate(neighbours.length * INT_SIZE);
		List<CrawlID> toQueue = new LinkedList<>();
		for (int n : neighbours) {
			toQueue.add(CrawlID.newBuilder().setId(""+n).build());
			buff.putInt(n);
		}
			
		interested.crawlDone(id, toQueue);
		buff.rewind();
		saver.save(id.getId(), buff.array());
	}
}
