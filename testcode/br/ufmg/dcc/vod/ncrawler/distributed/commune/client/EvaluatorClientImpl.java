package br.ufmg.dcc.vod.ncrawler.distributed.commune.client;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

public class EvaluatorClientImpl<I, C> implements EvaluatorClient<I, C>  {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(EvaluatorClientImpl.class);
	
	// Volatile since it will not be serialized remotely
	private volatile Evaluator<I, C> e;

	//Remote method
	@Override
	public void evaluteAndSave(I collectID, C collectContent) {
		LOG.info("Result received: "+ collectID);
		e.evaluteAndSave(collectID, collectContent);
	}
	
	//Local methods
	public void wrap(Evaluator<I, C> e) {
		this.e = e;
	}

	@Override
	public void error(I collectID, UnableToCollectException utce) {
		LOG.info("Result with error received"+ collectID);
		e.error(collectID, utce);
	}
}