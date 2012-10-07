package br.ufmg.dcc.vod.ncrawler.tracker;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Assert;

import org.junit.Test;

public class BloomFilterTrackerFactoryTest {

	@Test
	public void testValidCreations() {
		
		new BloomFilterTrackerFactory<String>().createTracker(
				String.class).crawled("oi");

		new BloomFilterTrackerFactory<CharSequence>().createTracker(
				CharSequence.class).crawled("oi");
		
		new BloomFilterTrackerFactory<Integer>().createTracker(
				Integer.class).crawled(1);
		
		new BloomFilterTrackerFactory<Long>().createTracker(
				Long.class).crawled(Long.MAX_VALUE);
		
		new BloomFilterTrackerFactory<byte[]>().createTracker(
				byte[].class).crawled(new byte[]{1, 2, 3});
		
	}

	@Test
	public void testInvalidCreation() {
		
		try {
			new BloomFilterTrackerFactory<Exception>().createTracker(
					Exception.class).crawled(new Exception());
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
