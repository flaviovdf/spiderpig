package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.common.collect.Sets;

public class UniMetropolisHastingRW 
		extends AbstractConfigurable<Void> implements ConfigurableWalker {
	
	private Map<CrawlID, IDStruct> nodes;
	private Random random;
	private long maxSteps;
	private long steps;

	public UniMetropolisHastingRW() {
		this.nodes = new HashMap<>();
		this.steps = 0;
	}
	
	@Override
	public List<CrawlID> getToWalk(CrawlID crawled, List<CrawlID> links) {
		
		if (this.steps == this.maxSteps)
			return Collections.emptyList();
		
		Set<CrawlID> affected = 
				initAndMarkReverseLinksDone(crawled, links);
		
		ArrayList<CrawlID> returnVal = new ArrayList<>();
		for (CrawlID id : affected) {
			IDStruct struct = getStruct(id);
			if (struct.isDoneWaiting()) {
				CrawlID next = getNextUndoneStep(id);
				returnVal.add(next);
			}
		}
		
		// Add undiscovered ids if necessary for continuing walk from crawled id
		if (getStruct(crawled).isWalkStep()) {
			this.steps++;
			for (CrawlID link : links)
				if (!returnVal.contains(link))
					returnVal.add(link);
		}
		
		return returnVal;
	}

	private CrawlID getNextUndoneStep(CrawlID id) {
		
		boolean stop = false;
		CrawlID next = id;
		while (!stop) {
			IDStruct struct = getStruct(next);
			
			/*
			 * Marks that we walked this node during the random walk.
			 * This is needed since some nodes are only used for probability
			 * computation
			 */
			struct.setWalkStepTrue(); 
			
			List<CrawlID> links = struct.getLinks();
			//reached a dead end or uncrawled id
			if (links.isEmpty() || !struct.isDoneWaiting()) 
				stop = true;
			else if (links.size() == 1) //can only go to this node
				next = links.get(0);
			else {
				int numLinksAt = links.size();
				
				CrawlID possibleStep = links.get(random.nextInt(numLinksAt));
				IDStruct possibleStepStruct = getStruct(possibleStep);
				
				int numLinksPossible = possibleStepStruct.getLinks().size();
	
				double walkProb = ((double) numLinksPossible) / numLinksAt;
				double randProb = random.nextDouble();
				if (randProb <= walkProb) //Walk
					next = possibleStep;
			}
		}

		return next;
	}

	private Set<CrawlID> initAndMarkReverseLinksDone(CrawlID id, 
			List<CrawlID> links) {
		
		Set<CrawlID> returnVal = Sets.newHashSet(links);
		returnVal.add(id);
		
		//Add links if ID has not been already seen
		IDStruct idStruct = getStruct(id);
		if (!idStruct.areLinksSet())
			idStruct.initLinks(links);

		//Add reverse links to self
		for (CrawlID link : links)
			getStruct(link).addReverseLink(id);
		
		//Mark any waiting links that have already been crawled
		for (CrawlID waitingLink : idStruct.getWaitingLinks()) {
			IDStruct waitingIdStruct = getStruct(waitingLink);
			if (waitingIdStruct.isDoneWaiting())
				idStruct.markLinkAsDone(waitingIdStruct.getID());
		}
		
		//Get reverse links and mark as done locally
		for (CrawlID reverseLink : idStruct.getReverseLinks()) {
			IDStruct reverseLinkStruct = getStruct(reverseLink);
			reverseLinkStruct.markLinkAsDone(id);
			returnVal.add(reverseLinkStruct.getID());
		}
		
		return returnVal;
	}

	private IDStruct getStruct(CrawlID id) {
		IDStruct struct = this.nodes.get(id);
		if (struct == null) {
			struct = new IDStruct(id);
			this.nodes.put(id, struct);
		}
		
		return struct;
	}

	@Override
	public void addSeedID(CrawlID seed) {
	}

	@Override
	public Void realConfigurate(Configuration configuration) throws Exception {
		configuration.setProperty(RandomWalker.STOP_PROB, 0);
		long seed = configuration.getLong(RandomWalker.RANDOM_SEED);
		if (seed != 0)
			this.random = new Random(seed);
		else
			this.random = new Random();
		
		this.maxSteps = configuration.getLong(RandomWalker.STEPS);
		
		return null;
	}

	@Override
	public Set<String> getRequiredParameters() {
		return new HashSet<String>(Arrays.asList(RandomWalker.STEPS, 
				RandomWalker.RANDOM_SEED));
	}
}