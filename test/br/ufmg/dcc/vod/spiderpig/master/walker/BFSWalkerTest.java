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

public class BFSWalkerTest {

	@Test
	public void testAll() throws Exception {
		ConfigurableBuilder configurableBuilder = new ConfigurableBuilder();
		BaseConfiguration configuration = new BaseConfiguration();
		configuration.addProperty(BFSWalker.BLOOM_INSERTS, 100);
		
		BFSWalker walker = 
				configurableBuilder.build(BFSWalker.class, configuration);
		
		CrawlID id1 = CrawlID.newBuilder().setId("1").build();
		
		try {
			walker.walk(id1, new ArrayList<CrawlID>());
			Assert.fail();
		} catch (RuntimeException e) {
		}
		
		walker.filterSeeds(Lists.newArrayList(id1));
		
		List<CrawlID> toWalk = 
				Lists.newArrayList(walker.walk(id1, 
						new ArrayList<CrawlID>()));
		
		Assert.assertTrue(toWalk.isEmpty());
		
		toWalk = Lists.newArrayList(walker.walk(id1, null));
		Assert.assertTrue(toWalk.isEmpty());
		
		
		CrawlID id2 = CrawlID.newBuilder().setId("2").build();
		CrawlID id3 = CrawlID.newBuilder().setId("3").build();
		CrawlID id4 = CrawlID.newBuilder().setId("4").build();
		
		List<CrawlID> links = Arrays.asList(id2, id3, id4);
		toWalk = Lists.newArrayList(walker.walk(id1, links));
		
		Assert.assertTrue(links.containsAll(toWalk));
		Assert.assertTrue(toWalk.containsAll(links));
		
		toWalk = Lists.newArrayList(
				walker.walk(id2, new ArrayList<CrawlID>()));
		Assert.assertTrue(toWalk.isEmpty());
		
		toWalk = Lists.newArrayList(
				walker.walk(id3, new ArrayList<CrawlID>()));
		Assert.assertTrue(toWalk.isEmpty());
		
		toWalk = Lists.newArrayList(
				walker.walk(id4, new ArrayList<CrawlID>()));
		Assert.assertTrue(toWalk.isEmpty());
		
		CrawlID id5 = CrawlID.newBuilder().setId("5").build();
		toWalk = Lists.newArrayList(
				walker.walk(id4, Arrays.asList(id3, id4, id5)));
		Assert.assertTrue(toWalk.contains(id5));
		Assert.assertEquals(1, toWalk.size());
	}

	
}
