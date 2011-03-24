package br.ufmg.dcc.vod.ncrawler.jobs.youtube.videoresp_api;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.CrawlJobStringSerializer;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class YoutubeResponseFactory implements EvaluatorFactory<String, YoutubeVideoDAOWResponse, CrawlJob> {

	private VideoResponseEvaluator e;
	private CrawlJobStringSerializer serializer;

	@Override
	public Evaluator<String, YoutubeVideoDAOWResponse> getEvaluator() {
		return e;
	}

	@Override
	public Serializer<CrawlJob> getSerializer() {
		return serializer;
	}

	@Override
	public void initiate(int threads, File saveFolder, long sleepTime, List<String> seeds) {
		this.e = new VideoResponseEvaluator(seeds, saveFolder);
		this.serializer = new CrawlJobStringSerializer(e);
	}

	@Override
	public void shutdown() {
	}
}
