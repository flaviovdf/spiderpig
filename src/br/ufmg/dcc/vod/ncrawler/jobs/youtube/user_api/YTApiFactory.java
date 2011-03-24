package br.ufmg.dcc.vod.ncrawler.jobs.youtube.user_api;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.CrawlJobStringSerializer;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class YTApiFactory implements EvaluatorFactory<String, YoutubeUserDAO, CrawlJob> {

	private YoutubeAPIEvaluator e;
	private CrawlJobStringSerializer serializer;

	@Override
	public Serializer<CrawlJob> getSerializer() {
		return serializer;
	}

	@Override
	public void initiate(int nThreads, File saveFolder, long sleepTime, List<String> seeds) {
		this.e = new YoutubeAPIEvaluator(seeds, saveFolder, sleepTime);
		this.serializer = new CrawlJobStringSerializer(e);
	}

	@Override
	public void shutdown() {
	}

	@Override
	public Evaluator<String, YoutubeUserDAO> getEvaluator() {
		return e;
	}
}