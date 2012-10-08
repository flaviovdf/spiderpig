package br.ufmg.dcc.vod.ncrawler.jobs.generic;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;

public class CrawlJobStringSerializer 
		extends AbstractArraySerializer<CrawlJob<String, ?>>{

	private final AbstractEvaluator<?> e;

	public CrawlJobStringSerializer(AbstractEvaluator<?> e) {
		super(1);
		this.e = e;
	}

	@Override
	public byte[][] getArrays(CrawlJob<String, ?> t) {
		return new byte[][]{t.getID().getBytes()};
	}

	@Override
	public CrawlJob<String, ?> setValueFromArrays(byte[][] bs) {
		return e.createJob(new String(bs[0]));
	}

}
