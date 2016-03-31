package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.configuration.Configuration;

import com.google.common.collect.Lists;

import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.ExhaustCondition;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

/**
 * Simulates a random walker which performs a limited number of steps. Moreover,
 * the walker may stop given a probability.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class RandomWalker implements ConfigurableWalker {

    private static final ExhaustCondition CONDITION = new ExhaustCondition();
    
    public static final String STEPS = "master.walkstrategy.rw.steps";
    public static final String STOP_PROB = "master.walkstrategy.rw.stopprob";
    public static final String RANDOM_SEED = "master.walkstrategy.rw.seed";
    
    private double stopProbability;
    private Random random;
    private long maxSteps;
    private AtomicLong steps;
    
    public RandomWalker() {
        this.steps = new AtomicLong(0);
    }
    
    @Override
    public Iterable<CrawlID> walk(CrawlID crawled, Iterable<CrawlID> links) {
        double pStop = this.random.nextDouble();
        this.steps.incrementAndGet();
        
        ArrayList<CrawlID> linksList = null;
        if (links != null)
            linksList = Lists.newArrayList(links);
        
        if (linksList == null || linksList.isEmpty() || 
                this.steps.get() == this.maxSteps || 
                pStop < this.stopProbability) {
            return Collections.emptyList();
        } else {
            int rand = this.random.nextInt(linksList.size());
            return Arrays.asList(linksList.get(rand));
        }
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
    public void configurate(Configuration configuration, 
            ConfigurableBuilder builder) throws BuildException {
        this.stopProbability = configuration.getDouble(STOP_PROB);
        
        long seed = configuration.getLong(RANDOM_SEED);
        if (seed != 0)
            this.random = new Random(seed);
        else
            this.random = new Random();
        
        this.maxSteps = configuration.getLong(STEPS);
    }

    @Override
    public Set<String> getRequiredParameters() {
        return new HashSet<String>(Arrays.asList(STEPS, STOP_PROB, 
                RANDOM_SEED));
    }
}