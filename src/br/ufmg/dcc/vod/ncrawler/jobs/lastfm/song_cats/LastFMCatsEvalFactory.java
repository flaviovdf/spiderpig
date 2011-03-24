package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.song_cats;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.CrawlJobStringSerializer;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class LastFMCatsEvalFactory implements EvaluatorFactory<String, List<Pair<String, String>>, CrawlJob> {

	private LastFMCategoryEvaluator e;
	private CrawlJobStringSerializer serializer;

	@Override
	public Evaluator<String, List<Pair<String, String>>> getEvaluator() {
		return e;
	}

	@Override
	public Serializer<CrawlJob> getSerializer() {
		return serializer;
	}

	@Override
	public void initiate(int threads, File saveFolder, long sleepTime,
			List<String> seeds) {
		this.e = new LastFMCategoryEvaluator(seeds, saveFolder);
		this.serializer = new CrawlJobStringSerializer(e);		
	}

	@Override
	public void shutdown() {
	}

}
