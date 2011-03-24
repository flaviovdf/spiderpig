package br.ufmg.dcc.vod.ncrawler.distributed.commune.client;

import br.edu.ufcg.lsd.commune.api.Remote;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

@Remote
public interface EvaluatorClient<I, C> {

	public static final String NAME = "EVAL_CLIENT";

	public void evaluteAndSave(I collectID, C collectContent);
	
	public void error(I collectID, UnableToCollectException utce);

}
