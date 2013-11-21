package br.ufmg.dcc.vod.spiderpig.master.walker.tracker;

/**
 * Decorates another tracker providing thread safe access.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 * @see {@link com.google.common.hash.BloomFilter} 
 */
public class ThreadSafeTracker<T> implements Tracker<T> {

    private final Tracker<T> tracker;

    /**
     * Creates this this tracker which will basically decorate the 
     * one given as parameter.
     * 
     * @param tracker {@code Tracker} to decorate
     */
    protected ThreadSafeTracker(Tracker<T> tracker) {
        this.tracker = tracker;
    }
    
    @Override
    public synchronized boolean addCrawled(T t) {
        return this.tracker.addCrawled(t);
    }

    @Override
    public synchronized boolean wasCrawled(T t) {
        return this.tracker.wasCrawled(t);
    }

    @Override
    public synchronized int numCrawled() {
        return this.tracker.numCrawled();
    }
    
}
