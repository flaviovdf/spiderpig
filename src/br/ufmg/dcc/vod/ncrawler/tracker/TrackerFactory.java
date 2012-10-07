package br.ufmg.dcc.vod.ncrawler.tracker;

/**
 * Interface for factories which can build trackers.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 * @param <T> Type of object which will be tracked
 */
public abstract class TrackerFactory<T> {

	/**
	 * Method to actually create the tracker. The {@code clazz} parameter
	 * is necessary because infering generic types at runtime is a hack.
	 * Some factories will need to deal with different types. 
	 * 
	 * @param clazz The generic Class
	 * @return The new tracker
	 */
	public abstract Tracker<T> createTracker(Class<T> clazz);
	
	/**
	 * Wraps created tracker using a {@link ThreadSafeTracker} for
	 * thread safety. 
	 * 
	 * @param clazz The generic Class
	 * @return a new tracker
	 */
	public Tracker<T> createThreadSafeTracker(Class<T> clazz) {
		Tracker<T> tr = new ThreadSafeTracker<T>(createTracker(clazz));
		return tr;
	}
	
}