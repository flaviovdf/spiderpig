package br.ufmg.dcc.vod.spiderpig.distributed.fd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.ufmg.dcc.vod.spiderpig.common.ServiceIDUtils;
import br.ufmg.dcc.vod.spiderpig.common.Tuple;
import br.ufmg.dcc.vod.spiderpig.distributed.nio.service.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.distributed.worker.FDServerActor;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.PingPong;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.queue.QueueService;

public class FDClientActorTest {

	private ArrayList<QueueService> services;

	@Before
	public void setUp() {
		services = new ArrayList<>();
	}
	
	@After
	public void tearDown() {
		for (QueueService s : services)
			s.waitUntilWorkIsDoneAndStop(1);
	}
	
	public Tuple<Set<ServiceID>, Set<FDServerActorX>> 
			startToMonitor(int numServers, int basePort) 
						throws IOException {
		RemoteMessageSender sender = new RemoteMessageSender();
		Set<ServiceID> sids = new HashSet<>();
		Set<FDServerActorX> actors = new HashSet<>();
		for (int i = 0; i < numServers; i++) {
			QueueService service = new QueueService("localhost", basePort + i);
			
			FDServerActorX fdactor = new FDServerActorX(sender);
			fdactor.withSimpleQueue(service).startProcessors(1);
			
			services.add(service);
			actors.add(fdactor);
			sids.add(ServiceIDUtils.toServiceID("localhost", basePort + i, 
					FDServerActor.HANDLE));
		}
		return new Tuple<Set<ServiceID>, Set<FDServerActorX>>(sids, actors);
	}
	
	private class FDServerActorX extends FDServerActor {

		private AtomicBoolean down = new AtomicBoolean(false);

		public FDServerActorX(RemoteMessageSender sender) {
			super(sender);
		}
		
		@Override
		public void process(PingPong t) {
			if (!this.down.get()) {
				super.process(t);
			}
		}
		
		public void setDown() {
			this.down.set(true);
		}
		
		public void setUp() {
			this.down.set(false);
		}
	}
	
	private class Listener implements FDListener {

		private final CountDownLatch upLatch;
		private final CountDownLatch downLatch;

		final List<ServiceID> ups = 
				Collections.synchronizedList(new ArrayList<ServiceID>());

		final List<ServiceID> downs = 
				Collections.synchronizedList(new ArrayList<ServiceID>());
		
		public Listener(CountDownLatch upLatch, CountDownLatch downLatch) {
			this.upLatch = upLatch;
			this.downLatch = downLatch;
		}
		
		@Override
		public void isUp(ServiceID serviceID) {
			ups.add(serviceID);
			this.upLatch.countDown();
		}

		@Override
		public void isSuspected(ServiceID serviceID) {
			downs.add(serviceID);
			this.downLatch.countDown();
		}
		
	}
	
	@Test
	public void testUpOne() throws IOException, InterruptedException {
		Tuple<Set<ServiceID>, Set<FDServerActorX>> tuple = 
				startToMonitor(1, 5000);
		Set<ServiceID> toMonitor = tuple.first;
		
		CountDownLatch upLatch = new CountDownLatch(1);
		CountDownLatch downLatch = new CountDownLatch(0);
		
		Listener listener = new Listener(upLatch, downLatch);
		FDClientActor actor = new FDClientActor(5, 1, TimeUnit.SECONDS, 
				listener, new RemoteMessageSender());
		QueueService service = new QueueService("localhost", 4000);
		actor.withSimpleQueue(service).startProcessors(1);
		for (ServiceID serviceID : toMonitor)
			actor.watch(serviceID);
		
		actor.startTimer();
		upLatch.await();
		downLatch.await();
		
		Assert.assertEquals(1, listener.ups.size());
		Assert.assertTrue(toMonitor.containsAll(listener.ups));
		service.waitUntilWorkIsDone(1);
		actor.stopTimer();
	}

	@Test
	public void testUp100() throws Exception {
		Tuple<Set<ServiceID>, Set<FDServerActorX>> tuple = 
				startToMonitor(100, 6000);
		Set<ServiceID> toMonitor = tuple.first;
		
		CountDownLatch upLatch = new CountDownLatch(100);
		CountDownLatch downLatch = new CountDownLatch(0);
		
		Listener listener = new Listener(upLatch, downLatch);
		FDClientActor actor = new FDClientActor(5, 1, TimeUnit.SECONDS, 
				listener, new RemoteMessageSender());
		QueueService service = new QueueService("localhost", 4001);
		actor.withSimpleQueue(service).startProcessors(1);
		for (ServiceID serviceID : toMonitor)
			actor.watch(serviceID);
		
		actor.startTimer();
		
		upLatch.await();
		downLatch.await();
		
		Assert.assertEquals(100, listener.ups.size());
		Assert.assertTrue(toMonitor.containsAll(listener.ups));
		
		service.waitUntilWorkIsDone(1);
		actor.stopTimer();
	}
	
	@Test
	public void testUpDown100() throws Exception {
		Tuple<Set<ServiceID>, Set<FDServerActorX>> tuple = 
				startToMonitor(100, 7000);
		Set<ServiceID> toMonitor = tuple.first;
		
		CountDownLatch upLatch = new CountDownLatch(100);
		CountDownLatch downLatch = new CountDownLatch(100);
		
		Listener listener = new Listener(upLatch, downLatch);
		FDClientActor actor = new FDClientActor(5, 1, TimeUnit.SECONDS, 
				listener, new RemoteMessageSender());
		QueueService service = new QueueService("localhost", 4002);
		actor.withSimpleQueue(service).startProcessors(1);
		for (ServiceID serviceID : toMonitor)
			actor.watch(serviceID);
		
		actor.startTimer();
		
		upLatch.await();
		
		Assert.assertEquals(100, listener.ups.size());
		Assert.assertTrue(toMonitor.containsAll(listener.ups));
		
		for (FDServerActorX fdax : tuple.second)
			fdax.setDown();
		
		downLatch.await();
		
		Assert.assertEquals(100, listener.downs.size());
		Assert.assertTrue(toMonitor.containsAll(listener.downs));
		
		service.waitUntilWorkIsDone(1);
		actor.stopTimer();
	}
	
	@Test
	public void testUpDownUp100() throws Exception {
		Tuple<Set<ServiceID>, Set<FDServerActorX>> tuple = 
				startToMonitor(100, 8000);
		Set<ServiceID> toMonitor = tuple.first;
		
		CountDownLatch upLatch = new CountDownLatch(200);
		CountDownLatch downLatch = new CountDownLatch(100);
		
		Listener listener = new Listener(upLatch, downLatch);
		FDClientActor actor = new FDClientActor(5, 1, TimeUnit.SECONDS, 
				listener, new RemoteMessageSender());
		QueueService service = new QueueService("localhost", 4003);
		actor.withSimpleQueue(service).startProcessors(1);
		for (ServiceID serviceID : toMonitor)
			actor.watch(serviceID);
		
		actor.startTimer();
		
		while (upLatch.getCount() > 100);
		
		Assert.assertEquals(100, upLatch.getCount());
		Assert.assertEquals(100, listener.ups.size());
		
		for (FDServerActorX fdax : tuple.second)
			fdax.setDown();
		
		downLatch.await();
		
		Assert.assertEquals(100, listener.downs.size());
		Assert.assertTrue(toMonitor.containsAll(listener.downs));

		for (FDServerActorX fdax : tuple.second)
			fdax.setUp();
		
		upLatch.await();
		
		Assert.assertEquals(100, listener.downs.size());
		Assert.assertEquals(200, listener.ups.size());
		
		service.waitUntilWorkIsDone(1);
		actor.stopTimer();
	}
}
