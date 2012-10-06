package br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator;

import java.nio.ByteBuffer;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractArraySerializer;

public class TestSerializer extends AbstractArraySerializer<TestCrawlJob> {

	private final RandomizedSyncGraph g;

	public TestSerializer(RandomizedSyncGraph g) {
		super(1);
		this.g = g;
	}

	@Override
	public byte[][] getArrays(TestCrawlJob t) {
		byte[] result = new byte[4];
		ByteBuffer wrap = ByteBuffer.wrap(result);
		wrap.putInt(t.vertex());
		
		return new byte[][]{result};
	}

	@Override
	public TestCrawlJob setValueFromArrays(byte[][] bs) {
		ByteBuffer wrap = ByteBuffer.wrap(bs[0]);
		return new TestCrawlJob(wrap.getInt(), g);
	}
}
