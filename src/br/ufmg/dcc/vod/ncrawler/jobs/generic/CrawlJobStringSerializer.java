package br.ufmg.dcc.vod.ncrawler.jobs.generic;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;

public class CrawlJobStringSerializer extends
	AbstractArraySerializer<CrawlJob>{

	private final AbstractEvaluator<?> e;

	public CrawlJobStringSerializer(AbstractEvaluator<?> e) {
		super(1);
		this.e = e;
	}

	@Override
	public byte[][] getArrays(CrawlJob t) {
		return new byte[][]{t.getID().getBytes()};
	}

	@Override
	public CrawlJob setValueFromArrays(byte[][] bs) {
		return e.createJob(new String(bs[0]));
	}

}
