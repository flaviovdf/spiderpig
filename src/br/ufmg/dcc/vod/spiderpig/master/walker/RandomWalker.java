package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

/**
 * Simulates a random walker which performs a limited number of steps. Moreover,
 * the walker may stop given a probability.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class RandomWalker extends AbstractConfigurable<Void> 
		implements ConfigurableWalker {

	public static final String STEPS = "master.walkstrategy.rw.steps";
	public static final String STOP_PROB = "master.walkstrategy.rw.stopprob";
	public static final String RANDOM_SEED = "master.walkstrategy.rw.seed";
	
	private double stopProbability;
	private Random random;
	private long maxSteps;
	private AtomicLong steps;
	private ArrayList<CrawlID> seed;
	
	public RandomWalker() {
		this.steps = new AtomicLong(0);
	}
	
	@Override
	public List<CrawlID> getToWalk(CrawlID crawled, List<CrawlID> links) {
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
	public void addSeedID(CrawlID seed) {
		this.seed.add(seed);
	}
	
	@Override
	public List<CrawlID> getSeedDispatch() {
		return this.seed;
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
		this.seed = new ArrayList<>();
		return null;
	}

	@Override
	public Set<String> getRequiredParameters() {
		return new HashSet<String>(Arrays.asList(STEPS, STOP_PROB, 
				RANDOM_SEED));
	}
}