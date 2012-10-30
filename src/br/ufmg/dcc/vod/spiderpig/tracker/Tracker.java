package br.ufmg.dcc.vod.spiderpig.tracker;

/**
 * A {@code Tracker} object keeps track of crawled objects. These IDs objects be
 * {@code URLs}, {@code Strings}, {@code Byte Arrays} etc. 
 *  
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 * @param <T> Type which represents a crawled object
 */
public interface Tracker<T> {
	
	/**
	 * Indicates to the tracker that {@code s} was crawled.
	 * 
	 * @param t The object which was crawled
	 * @return {@code true} if the obj was not yet crawled, {@false} otherwise
	 */
	public boolean crawled(T t);
	
	/**
	 * Queries whether {@code s} has already been crawled.
	 * 
	 * @param t The object which was crawled
	 * @return {@code true} if it was, {@false} otherwise
	 */
	public boolean wasCrawled(T t);
	
	/**
	 * @return The number of objects which are being tracked.
	 */
	public int numCrawled();
	
}
