package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.ExhaustCondition;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

/**
 * Simulates a random walker which performs a limited number of steps. Moreover,
 * the walker may stop given a probability.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class RandomWalker extends AbstractWalker {

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
	protected List<CrawlID> getToWalkImpl(CrawlID crawled, List<CrawlID> links) {
		double pStop = this.random.nextDouble();
		this.steps.incrementAndGet();
		if (links == null || links.isEmpty() || 
				this.steps.get() == this.maxSteps || 
				pStop < this.stopProbability) {
			return Collections.emptyList();
		} else {
			int rand = this.random.nextInt(links.size());
			return Arrays.asList(links.get(rand));
		}
	}

	@Override
	protected List<CrawlID> filterSeeds(List<CrawlID> seeds) {
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
	public Void realConfigurate(Configuration configuration) {
		this.stopProbability = configuration.getDouble(STOP_PROB);
		
		long seed = configuration.getLong(RANDOM_SEED);
		if (seed != 0)
			this.random = new Random(seed);
		else
			this.random = new Random();
		
		this.maxSteps = configuration.getLong(STEPS);
		return null;
	}

	@Override
	public Set<String> getRequiredParameters() {
		return new HashSet<String>(Arrays.asList(STEPS, STOP_PROB, 
				RANDOM_SEED));
	}
}