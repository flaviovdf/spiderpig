package br.ufmg.dcc.vod.spiderpig.master.walker.tracker;

import java.util.HashMap;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;

/**
 * Factory to crate {@code BloomFilter} trackers. By defautlt, the bloom filter
 * will have an expected number of insertions of ten million with .03 failure
 * probability.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 * @param <T> Type of object to store
 * @see {@link com.google.common.hash.BloomFilter} 
 */
public class BloomFilterTrackerFactory<T> extends TrackerFactory<T> {

    private static final HashMap<Class<?>, Funnel<?>> FUNNELS = new HashMap<>();
    private static final int TEN_MILLION = 10000000;
    private final int expectedInserts;
    
    static {
        FUNNELS.put(String.class, Funnels.stringFunnel());
        FUNNELS.put(CharSequence.class, Funnels.stringFunnel());
        FUNNELS.put(byte[].class, Funnels.byteArrayFunnel());
        FUNNELS.put(Integer.class, Funnels.integerFunnel());
        FUNNELS.put(Long.class, Funnels.longFunnel());
    }
    
    /**
     * Creates a tracker factory where each bloom filter will expect the
     * given amount of inserts
     * 
     * @param expectedInserts Number of inserts expected
     */
    public BloomFilterTrackerFactory(int expectedInserts) {
        this.expectedInserts = expectedInserts;
    }

    /**
     * Creates a tracker factory where each bloom filter will expect 10million
     * inserts
     */
    public BloomFilterTrackerFactory() {
        this(TEN_MILLION);
    }
    
    @Override
    public Tracker<T> createTracker(Class<T> clazz) {
        if (!FUNNELS.containsKey(clazz)) {
            throw new InstatiationException(String.format(
                    "Only %s classes can be used as parameters", 
                    FUNNELS.keySet().toString())); 
        }
        
        @SuppressWarnings("unchecked") Funnel<T> funnel =
                (Funnel<T>) FUNNELS.get(clazz);
        BloomFilter<T> bf = BloomFilter.create(funnel, expectedInserts);
        return new BloomFilterTracker<>(bf);
    }

}