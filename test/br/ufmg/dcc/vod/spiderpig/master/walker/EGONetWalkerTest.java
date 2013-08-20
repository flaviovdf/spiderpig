package br.ufmg.dcc.vod.spiderpig.master.walker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.BaseConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import br.ufmg.dcc.vod.spiderpig.common.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.common.collect.Lists;

public class EGONetWalkerTest {

	@Test
	public void testOneLevel() throws Exception {
		EGONetWalker walker = new EGONetWalker();
		ProcessorActor mock = Mockito.mock(ProcessorActor.class);
		mock.withSimpleQueue(new QueueService());
		walker.setProcessorActor(mock);
		
		BaseConfiguration configuration = new BaseConfiguration();
		configuration.addProperty(BFSWalker.BLOOM_INSERTS, 100);
		configuration.addProperty(EGONetWalker.NUM_NETS, 1);
		walker.configurate(configuration);
		
		CrawlID id1 = CrawlID.newBuilder().setId("1").build();
		
		try {
			walker.getToWalkImpl(id1, new ArrayList<CrawlID>());
			Assert.fail();
		} catch (RuntimeException e) {
		}
		
		walker.setSeeds(Lists.newArrayList(id1));
		walker.dispatchSeeds();
		
		List<CrawlID> toWalk = Lists.newArrayList(
				walker.getToWalkImpl(id1, new ArrayList<CrawlID>()));
		Assert.assertTrue(toWalk.isEmpty());
		
		toWalk = Lists.newArrayList(walker.getToWalkImpl(id1, null));
		Assert.assertTrue(toWalk.isEmpty());
		
		CrawlID id2 = CrawlID.newBuilder().setId("2").build();
		CrawlID id3 = CrawlID.newBuilder().setId("3").build();
		CrawlID id4 = CrawlID.newBuilder().setId("4").build();
		
		List<CrawlID> links = Arrays.asList(id2, id3, id4);
		toWalk = Lists.newArrayList(walker.getToWalkImpl(id1, links));
		
		Assert.assertTrue(links.containsAll(toWalk));
		Assert.assertTrue(toWalk.containsAll(links));
	}

	@Test
	public void testZeroLevel() throws Exception {
		EGONetWalker walker = new EGONetWalker();
		ProcessorActor mock = Mockito.mock(ProcessorActor.class);
		mock.withSimpleQueue(new QueueService());
		walker.setProcessorActor(mock);
		
		BaseConfiguration configuration = new BaseConfiguration();
		configuration.addProperty(BFSWalker.BLOOM_INSERTS, 100);
		configuration.addProperty(EGONetWalker.NUM_NETS, 0);
		walker.configurate(configuration);
		
		CrawlID id1 = CrawlID.newBuilder().setId("1").build();
		walker.setSeeds(Lists.newArrayList(id1));
		walker.dispatchSeeds();
		
		List<CrawlID> toWalk = Lists.newArrayList(
				walker.getToWalkImpl(id1, new ArrayList<CrawlID>()));
		Assert.assertTrue(toWalk.isEmpty());
		
		toWalk = Lists.newArrayList(
				walker.getToWalkImpl(id1, null));
		Assert.assertTrue(toWalk.isEmpty());
		
		CrawlID id2 = CrawlID.newBuilder().setId("2").build();
		CrawlID id3 = CrawlID.newBuilder().setId("3").build();
		CrawlID id4 = CrawlID.newBuilder().setId("4").build();
		
		List<CrawlID> links = Arrays.asList(id2, id3, id4);
		toWalk = Lists.newArrayList(walker.getToWalkImpl(id1, links));
		Assert.assertTrue(toWalk.isEmpty());
	}
	
	@Test
	public void testTwoLevel() throws Exception {
		EGONetWalker walker = new EGONetWalker();
		ProcessorActor mock = Mockito.mock(ProcessorActor.class);
		mock.withSimpleQueue(new QueueService());
		walker.setProcessorActor(mock);
		
		BaseConfiguration configuration = new BaseConfiguration();
		configuration.addProperty(BFSWalker.BLOOM_INSERTS, 100);
		configuration.addProperty(EGONetWalker.NUM_NETS, 2);
		walker.configurate(configuration);
		
		CrawlID id1 = CrawlID.newBuilder().setId("1").build();
		CrawlID id2 = CrawlID.newBuilder().setId("2").build();
		
		walker.setSeeds(Lists.newArrayList(id1, id2));
		walker.dispatchSeeds();
		
		CrawlID id3 = CrawlID.newBuilder().setId("3").build();
		CrawlID id4 = CrawlID.newBuilder().setId("4").build();
		CrawlID id5 = CrawlID.newBuilder().setId("5").build();
		
		List<CrawlID> links1 = Arrays.asList(id2, id3);
		List<CrawlID> links2 = Arrays.asList(id1, id3, id4, id5);
		
		List<CrawlID> toWalk = Lists.newArrayList(
				walker.getToWalkImpl(id1, links1));
		Assert.assertEquals(1, toWalk.size());
		Assert.assertTrue(toWalk.contains(id3));
		
		toWalk = Lists.newArrayList(
				walker.getToWalkImpl(id2, links2));
		Assert.assertEquals(2, toWalk.size());
		Assert.assertTrue(toWalk.contains(id4));
		Assert.assertTrue(toWalk.contains(id5));
		
		CrawlID id6 = CrawlID.newBuilder().setId("6").build();
		List<CrawlID> linksEmpty = new ArrayList<>();
		List<CrawlID> links5 = Arrays.asList(id6);
		
		toWalk = Lists.newArrayList(
				walker.getToWalkImpl(id3, linksEmpty));
		Assert.assertTrue(toWalk.isEmpty());
		
		toWalk = 
				Lists.newArrayList(walker.getToWalkImpl(id4, linksEmpty));
		Assert.assertTrue(toWalk.isEmpty());
		
		//Finished all levels
		toWalk = 
				Lists.newArrayList(walker.getToWalkImpl(id5, links5));
		Assert.assertEquals(1, toWalk.size());
		
		//Will now ignore new links
		List<CrawlID> links6 = Arrays.asList(id1, id2, id3, id4, id5);
		toWalk = 
				Lists.newArrayList(walker.getToWalkImpl(id6, links6));
		Assert.assertTrue(toWalk.isEmpty());
	}
}
