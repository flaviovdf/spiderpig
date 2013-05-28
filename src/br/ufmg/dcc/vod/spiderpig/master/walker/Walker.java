package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.List;

import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

/**
 * A walk is responsible for defining the network walk strategy for
 * crawlers. Basically, implementors of this class will return a list
 * of ids to be queued for crawling based on the result of a previous crawled
 * id. 
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface Walker {

	/**
	 * Indicates that the current id was crawled and dispatches new ids based 
	 * on current result
	 * 
	 * @param crawled ID crawled.
	 * @param links Links discovered
	 */
	public void dispatchNext(CrawlID crawled, List<CrawlID> links);
	
	/**
	 * Indicates to the walker that the following id produced an error.
	 * 
	 * @param idWithError ID crawled.
	 */
	public void errorReceived(CrawlID idWithError);

	/**
	 * Indicates to the walker that the following id was not crawled because
	 * a worker has died. The default approach here is to re-dispatch the id.
	 * 
	 * @param id ID crawled.
	 */
	public void workerFailedWithID(CrawlID id);
	
	/**
	 * Add seed ID the walker. Seeds are initial ids which may require special
	 * treatment by different walkers.
	 * 
	 * @param seed seed ID.
	 */
	public void addSeedID(CrawlID seed);

	/**
	 * After seeds are indicated, this method will dispatch the ids which
	 * should be crawled.
	 */
	public void dispatchSeeds();
	
	/**
	 * Sets the processor actor which will crawl ids.
	 * 
	 * @param processorActor {@link ProcessorActor} to crawl ids
	 */
	public void setProcessorActor(ProcessorActor processorActor);
	
	/**
	 * Gets the {@link StopCondition} which can indicate when the crawl is
	 * done.
	 * 
	 * @return {@link StopCondition} implementation
	 */
	public StopCondition getStopCondition();
	
}