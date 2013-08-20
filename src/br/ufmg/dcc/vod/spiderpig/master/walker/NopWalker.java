package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.ExhaustCondition;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

/**
 * A walker which follows no links. Useful for when crawling just a static set
 * of pages.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class NopWalker extends AbstractWalker {

	@Override
	protected Iterable<CrawlID> getToWalkImpl(CrawlID crawled, 
			Iterable<CrawlID> links) {
		return Collections.emptyList();
	}
	
	@Override
	protected Iterable<CrawlID> filterSeeds(Iterable<CrawlID> seeds) {
		return seeds;
	}

	@Override
	protected void errorReceivedImpl(CrawlID crawled) {
	}

	@Override
	protected StopCondition createStopCondition() {
		return new ExhaustCondition();
	}

	@Override
	public Set<String> getRequiredParameters() {
		return Collections.emptySet();
	}

	@Override
	public Void realConfigurate(Configuration configuration) throws Exception {
		return null;
	}

	
}