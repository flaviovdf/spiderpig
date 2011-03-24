package br.ufmg.dcc.vod.ncrawler.evaluator;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public interface EvaluatorFactory<I, C, T> {

	public void initiate(int threads, File saveFolder, long sleepTime, List<String> seeds)  throws Exception;
	
	public Evaluator<I, C> getEvaluator();

	public Serializer<T> getSerializer();

	public void shutdown() throws Exception;
	
}
