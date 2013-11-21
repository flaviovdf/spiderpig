package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.ExhaustCondition;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.master.walker.tracker.BloomFilterTrackerFactory;
import br.ufmg.dcc.vod.spiderpig.master.walker.tracker.Tracker;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

/**
 * A walker based on breadth first search. A {@link Tracker} is used to keep
 * track of previously crawled ids.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class BFSWalker implements ConfigurableWalker {

    private static final ExhaustCondition CONDITION = new ExhaustCondition();

    public static final String BLOOM_INSERTS = 
            "master.walkstrategy.bloomfilter_expected_inserts";

    private Tracker<String> tracker;
    
    @Override
    public Iterable<CrawlID> walk(CrawlID crawled, Iterable<CrawlID> links) {
        
        List<CrawlID> rv = new ArrayList<>();

        if (!tracker.wasCrawled(crawled.getId())) {
            throw new RuntimeException("ID not foun in tracker. This can"
                    + " only happen if id was not signalled as seed");
        }

        if (links != null)
            for (CrawlID link : links) {
                if (this.tracker.addCrawled(link.getId())) {
                    rv.add(link);
                }
            }

        return rv;
    }

    @Override
    public Iterable<CrawlID> filterSeeds(Iterable<CrawlID> seeds) {
        List<CrawlID> toDispatch = new ArrayList<>();
        for (CrawlID seed : seeds)
            if (tracker.addCrawled((seed.getId())))
                toDispatch.add(seed);

        return toDispatch;
    }

    @Override
    public void errorReceived(CrawlID crawled) {
        tracker.wasCrawled(crawled.getId());
    }

    @Override
    public StopCondition getStopCondition() {
        return CONDITION;
    }

    @Override
    public void configurate(Configuration configuration, 
            ConfigurableBuilder builder) throws BuildException {
        int expectedInserts = configuration.getInt(BLOOM_INSERTS);
        this.tracker = new BloomFilterTrackerFactory<String>(expectedInserts)
                .createTracker(String.class);
    }

    @Override
    public Set<String> getRequiredParameters() {
        return new HashSet<String>(Arrays.asList(BLOOM_INSERTS));
    }
}
