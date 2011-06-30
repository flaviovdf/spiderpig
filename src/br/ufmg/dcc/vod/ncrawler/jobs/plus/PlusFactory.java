package br.ufmg.dcc.vod.ncrawler.jobs.plus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.CrawlJobStringSerializer;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class PlusFactory implements EvaluatorFactory<String, Pair<PlusDao, PlusDao>, CrawlJob> {

	private PlusEvaluator eval;
	private CrawlJobStringSerializer serial;
	
	@Override
	public Evaluator<String, Pair<PlusDao, PlusDao>> getEvaluator() {
		return eval;
	}

	@Override
	public Serializer<CrawlJob> getSerializer() {
		return serial;
	}

	@Override
	public void initiate(int threads, File saveFolder, long sleepTime,	List<String> seeds) throws FileNotFoundException, IOException {
		this.eval = new PlusEvaluator(seeds, saveFolder);
		this.serial = new CrawlJobStringSerializer(this.eval);
	}

	@Override
	public void shutdown() throws IOException {
	}
}
