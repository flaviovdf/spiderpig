package br.ufmg.dcc.vod.ncrawler.jobs.urlsaver;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.CrawlJobStringSerializer;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class URLDownFactory implements EvaluatorFactory<String, byte[], CrawlJob> {

	private URLDownEvaluator eval;
	private CrawlJobStringSerializer serial;

	@Override
	public Evaluator<String, byte[]> getEvaluator() {
		return eval;
	}

	@Override
	public Serializer<CrawlJob> getSerializer() {
		return serial;
	}

	@Override
	public void initiate(int threads, File saveFolder, long sleepTime,
			List<String> seeds) throws Exception {
		this.eval = new URLDownEvaluator(seeds, saveFolder);
		this.serial = new CrawlJobStringSerializer(this.eval);		
	}

	@Override
	public void shutdown() throws Exception {
	}

}
