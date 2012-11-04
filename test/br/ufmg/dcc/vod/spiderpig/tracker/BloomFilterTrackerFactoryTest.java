package br.ufmg.dcc.vod.spiderpig.tracker;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Test;

import br.ufmg.dcc.vod.spiderpig.tracker.BloomFilterTrackerFactory;
import br.ufmg.dcc.vod.spiderpig.tracker.InstatiationException;
import br.ufmg.dcc.vod.spiderpig.tracker.ThreadSafeTracker;
import br.ufmg.dcc.vod.spiderpig.tracker.Tracker;

public class BloomFilterTrackerFactoryTest {

	@Test
	public void testValidCreations() {
		
		new BloomFilterTrackerFactory<String>().createTracker(
				String.class).addCrawled("oi");

		new BloomFilterTrackerFactory<CharSequence>().createTracker(
				CharSequence.class).addCrawled("oi");
		
		new BloomFilterTrackerFactory<Integer>().createTracker(
				Integer.class).addCrawled(1);
		
		new BloomFilterTrackerFactory<Long>().createTracker(
				Long.class).addCrawled(Long.MAX_VALUE);
		
		new BloomFilterTrackerFactory<byte[]>().createTracker(
				byte[].class).addCrawled(new byte[]{1, 2, 3});
		
	}

	@Test
	public void testInvalidCreation() {
		
		try {
			new BloomFilterTrackerFactory<Exception>().createTracker(
					Exception.class).addCrawled(new Exception());
			Assert.fail();
		} catch (InstatiationException e) {
		}
	}
	
	
	@Test
	public void testThreadSafeCreation() {
		Tracker<String> bf = 
				new BloomFilterTrackerFactory<String>().createThreadSafeTracker(
						String.class);
		
		Assert.assertTrue(bf instanceof ThreadSafeTracker);
		Method[] methods = bf.getClass().getDeclaredMethods();
		for (Method m : methods) {
			Assert.assertTrue(Modifier.isSynchronized(m.getModifiers()));
		}
	}
}
