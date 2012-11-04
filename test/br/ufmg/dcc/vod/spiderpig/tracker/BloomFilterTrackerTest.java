package br.ufmg.dcc.vod.spiderpig.tracker;

import junit.framework.Assert;

import org.junit.Test;

import br.ufmg.dcc.vod.spiderpig.tracker.BloomFilterTrackerFactory;
import br.ufmg.dcc.vod.spiderpig.tracker.Tracker;

public class BloomFilterTrackerTest {

	@Test
	public void testAll() {
		Tracker<String> bf = 
				new BloomFilterTrackerFactory<String>().createTracker(
						String.class);
		
		Assert.assertTrue(bf.addCrawled("oi"));
		Assert.assertTrue(bf.addCrawled("tudo"));
		Assert.assertTrue(bf.addCrawled("bem"));
		
		Assert.assertEquals(3, bf.numCrawled());
		Assert.assertTrue(bf.wasCrawled("oi"));
		Assert.assertTrue(bf.wasCrawled("tudo"));
		Assert.assertTrue(bf.wasCrawled("bem"));
		Assert.assertFalse(bf.wasCrawled("bala"));
		
		Assert.assertFalse(bf.addCrawled("oi"));
		Assert.assertEquals(3, bf.numCrawled());
	}

}
