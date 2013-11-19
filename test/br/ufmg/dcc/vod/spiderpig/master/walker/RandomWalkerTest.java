package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.BaseConfiguration;
import org.junit.Assert;
import org.junit.Test;

import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.common.collect.Lists;

public class RandomWalkerTest {

	@Test
	public void testProb0() throws Exception {
		ConfigurableBuilder configurableBuilder = new ConfigurableBuilder();
		BaseConfiguration configuration = new BaseConfiguration();
		
		configuration.addProperty(RandomWalker.RANDOM_SEED, 3);
		configuration.addProperty(RandomWalker.STEPS, 5);
		configuration.addProperty(RandomWalker.STOP_PROB, 0);
		
		RandomWalker walker = 
				configurableBuilder.build(RandomWalker.class, configuration);
		
		CrawlID id1 = CrawlID.newBuilder().setId("1").build();
		List<CrawlID> toWalk = 
				Lists.newArrayList(walker.walk(id1, 
						new ArrayList<CrawlID>()));
		Assert.assertTrue(toWalk.isEmpty());
		
		toWalk = Lists.newArrayList(
				walker.walk(id1, null));
		Assert.assertTrue(toWalk.isEmpty());
		
		CrawlID id2 = CrawlID.newBuilder().setId("2").build();
		CrawlID id3 = CrawlID.newBuilder().setId("3").build();
		CrawlID id4 = CrawlID.newBuilder().setId("4").build();
		
		toWalk = Lists.newArrayList(
				walker.walk(id2, Arrays.asList(id3, id4)));
		Assert.assertEquals(1, toWalk.size());
		Assert.assertTrue(toWalk.contains(id3) || toWalk.contains(id4));
		
		toWalk = Lists.newArrayList(
				walker.walk(toWalk.get(0), Arrays.asList(id1)));
		Assert.assertEquals(1, toWalk.size());
		Assert.assertTrue(toWalk.contains(id1));
		
		toWalk = Lists.newArrayList(
				walker.walk(id1, Arrays.asList(id1, id2, id3)));
		Assert.assertTrue(toWalk.isEmpty());
	}
	
	@Test
	public void testProb1() throws Exception {
		ConfigurableBuilder configurableBuilder = new ConfigurableBuilder();
		BaseConfiguration configuration = new BaseConfiguration();
		
		configuration.addProperty(RandomWalker.RANDOM_SEED, 3);
		configuration.addProperty(RandomWalker.STEPS, 1000);
		configuration.addProperty(RandomWalker.STOP_PROB, 1);
		
		RandomWalker walker = 
				configurableBuilder.build(RandomWalker.class, configuration);
		
		CrawlID id1 = CrawlID.newBuilder().setId("1").build();
		CrawlID id2 = CrawlID.newBuilder().setId("2").build();
		CrawlID id3 = CrawlID.newBuilder().setId("3").build();
		CrawlID id4 = CrawlID.newBuilder().setId("4").build();
		
		List<CrawlID> links = Arrays.asList(id1, id2, id3, id4);
		
		List<CrawlID> toWalk = Lists.newArrayList(
				walker.walk(id1, links));
		Assert.assertTrue(toWalk.isEmpty());
		
		toWalk = Lists.newArrayList(
				walker.walk(id2, links));
		Assert.assertTrue(toWalk.isEmpty());
	}
}