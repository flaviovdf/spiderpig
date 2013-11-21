package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.ExhaustCondition;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

/**
 * A walker which follows no links. Useful for when crawling just a static set
 * of pages.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class NopWalker implements ConfigurableWalker {

    private static final ExhaustCondition CONDITION = new ExhaustCondition();

    @Override
    public Iterable<CrawlID> walk(CrawlID crawled, Iterable<CrawlID> links) {
        return Collections.emptyList();
    }
    
    @Override
    public Iterable<CrawlID> filterSeeds(Iterable<CrawlID> seeds) {
        return seeds;
    }

    @Override
    public void errorReceived(CrawlID crawled) {
    }

    @Override
    public StopCondition getStopCondition() {
        return CONDITION;
    }

    @Override
    public Set<String> getRequiredParameters() {
        return Collections.emptySet();
    }

    public void configurate(Configuration configuration, 
            ConfigurableBuilder builder) throws BuildException {
    }   
}