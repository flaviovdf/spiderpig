package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.ExhaustCondition;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

/**
 * A walker which follows no links. Useful for when crawling just a static set
 * of pages.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class NopWalker extends AbstractConfigurable<Void> 
		implements ConfigurableWalker {

	private final StopCondition stopCondition = new ExhaustCondition();
	private final List<CrawlID> seed = new ArrayList<CrawlID>();
	
	@Override
	public List<CrawlID> getToWalk(CrawlID crawled, List<CrawlID> links) {
		return Collections.emptyList();
	}

	@Override
	public void addSeedID(CrawlID seedId) {
		seed.add(seedId);
	}

	@Override
	public List<CrawlID> getSeedDispatch() {
		return seed;
	}

	@Override
	public StopCondition getStopCondition() {
		return stopCondition;
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