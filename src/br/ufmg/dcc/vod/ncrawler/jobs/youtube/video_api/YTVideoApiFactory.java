package br.ufmg.dcc.vod.ncrawler.jobs.youtube.video_api;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.CrawlJobStringSerializer;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class YTVideoApiFactory implements EvaluatorFactory<String, YoutubeVideoDAO, CrawlJob> {

	private YoutubeVideoAPIEvaluator e;
	private CrawlJobStringSerializer serializer;

	@Override
	public Evaluator<String, YoutubeVideoDAO> getEvaluator() {
		return e;
	}

	@Override
	public Serializer<CrawlJob> getSerializer() {
		return serializer;
	}

	@Override
	public void initiate(int threads, File saveFolder, long sleepTime,
			List<String> seeds) {
		this.e = new YoutubeVideoAPIEvaluator(seeds, saveFolder);
		this.serializer = new CrawlJobStringSerializer(e);		
	}

	@Override
	public void shutdown() {
	}
}
