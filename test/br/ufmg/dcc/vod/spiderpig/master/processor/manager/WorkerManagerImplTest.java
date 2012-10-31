package br.ufmg.dcc.vod.spiderpig.master.processor.manager;

import java.util.Collection;
import java.util.HashSet;

import junit.framework.Assert;

import org.junit.Test;

import br.ufmg.dcc.vod.spiderpig.common.ServiceIDUtils;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.WorkerManagerImpl.WorkerState;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

public class WorkerManagerImplTest {

	private Collection<ServiceID> createIDs(int n) {
		HashSet<ServiceID> set = new HashSet<>();
		for (int i = 0; i < n; i++)
			set.add(buildSID(i));
		return set;
	}

	private ServiceID buildSID(int i) {
		return ServiceIDUtils.toServiceID("", i, ""+i);
	}
	
	@Test
	public void testCreation() {
		Collection<ServiceID> ids = createIDs(10);
		WorkerManagerImpl wmi = new WorkerManagerImpl(ids, WorkerState.IDLE);
		Collection<ServiceID> idle = wmi.getByState(WorkerState.IDLE);
		Collection<ServiceID> busy = wmi.getByState(WorkerState.BUSY);
		Collection<ServiceID> susp = wmi.getByState(WorkerState.SUSPECTED);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(10, idle.size());
		Assert.assertTrue(idle.containsAll(ids));
	}
	
	@Test
	public void testAllocateAvailableExecutor() throws InterruptedException {
		Collection<ServiceID> ids = createIDs(10);
		
		WorkerManagerImpl wmi = new WorkerManagerImpl(ids, WorkerState.IDLE);
		
		Collection<ServiceID> idle = wmi.getByState(WorkerState.IDLE);
		Collection<ServiceID> busy = wmi.getByState(WorkerState.BUSY);
		Collection<ServiceID> susp = wmi.getByState(WorkerState.SUSPECTED);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(10, idle.size());
		
		ServiceID wid = wmi.allocateAvailableExecutor(build("1"));
		
		Assert.assertEquals(1, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(9, idle.size());
		Assert.assertTrue(busy.contains(wid));
		
		Assert.assertNull(wmi.allocateAvailableExecutor(build("1")));
		
		ServiceID wid2 = wmi.allocateAvailableExecutor(build("2"));
		
		Assert.assertEquals(2, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(8, idle.size());
		Assert.assertTrue(busy.contains(wid));
		Assert.assertTrue(busy.contains(wid2));
	}

	private class Allocator extends Thread {
		private final WorkerManager wm;
		private final CrawlID id;

		public Allocator(WorkerManager wm, CrawlID id) {
			this.wm = wm;
			this.id = id;
		}
		
		@Override
		public void run() {
			try {
				wm.allocateAvailableExecutor(id);
			} catch (InterruptedException e) {
			}
		}
	}
	
	@Test
	public void testFreeExecutor() throws InterruptedException {
		Collection<ServiceID> ids = createIDs(10);
		
		WorkerManagerImpl wmi = new WorkerManagerImpl(ids, WorkerState.IDLE);
		
		Collection<ServiceID> idle = wmi.getByState(WorkerState.IDLE);
		Collection<ServiceID> busy = wmi.getByState(WorkerState.BUSY);
		Collection<ServiceID> susp = wmi.getByState(WorkerState.SUSPECTED);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(10, idle.size());
		
		wmi.allocateAvailableExecutor(build("1"));
		wmi.allocateAvailableExecutor(build("2"));
		
		Assert.assertEquals(2, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(8, idle.size());
		
		Assert.assertTrue(wmi.freeExecutor(build("2")));
		
		Assert.assertEquals(1, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(9, idle.size());
		
		Assert.assertTrue(wmi.freeExecutor(build("1")));
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(10, idle.size());
		
		Assert.assertFalse(wmi.freeExecutor(build("1")));
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(10, idle.size());
		Assert.assertTrue(idle.containsAll(ids));
	}

	@Test
	public void testThreadSafety1() throws InterruptedException {
		Collection<ServiceID> ids = createIDs(2);

		WorkerManagerImpl wmi = new WorkerManagerImpl(ids, WorkerState.IDLE);
		
		wmi.allocateAvailableExecutor(build("1"));
		wmi.allocateAvailableExecutor(build("2"));
		
		Collection<ServiceID> busy = wmi.getByState(WorkerState.BUSY);
		Assert.assertEquals(2, busy.size());
		
		Allocator allocator = new Allocator(wmi, build("3"));
		allocator.start();
		
		while(allocator.getState() != Thread.State.WAITING);
		
		wmi.freeExecutor(build("1"));
		
		Assert.assertEquals(1, busy.size());
		
		allocator.join();
		
		Assert.assertEquals(2, busy.size());
	}
	
	@Test
	public void testExecutorSuspected() throws InterruptedException {
		Collection<ServiceID> ids = createIDs(10);
		
		WorkerManagerImpl wmi = new WorkerManagerImpl(ids, WorkerState.IDLE);
		
		Collection<ServiceID> idle = wmi.getByState(WorkerState.IDLE);
		Collection<ServiceID> busy = wmi.getByState(WorkerState.BUSY);
		Collection<ServiceID> susp = wmi.getByState(WorkerState.SUSPECTED);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(10, idle.size());
		
		wmi.executorSuspected(buildSID(0));
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(1, susp.size());
		Assert.assertEquals(9, idle.size());
		
		Assert.assertTrue(susp.contains(buildSID(0)));
		Assert.assertFalse(idle.contains(buildSID(0)));
		
		ServiceID wid = wmi.allocateAvailableExecutor(build("1"));
		
		Assert.assertEquals(1, busy.size());
		Assert.assertEquals(1, susp.size());
		Assert.assertEquals(8, idle.size());
		
		Assert.assertTrue(susp.contains(buildSID(0)));
		Assert.assertFalse(idle.contains(buildSID(0)));
		
		wmi.executorSuspected(wid);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(2, susp.size());
		Assert.assertEquals(8, idle.size());
		
		wmi.executorSuspected(wid);

		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(2, susp.size());
		Assert.assertEquals(8, idle.size());
		
		Assert.assertTrue(susp.contains(wid));
	}

	@Test
	public void testMarkAvailable() throws InterruptedException {
		Collection<ServiceID> ids = createIDs(10);
		
		WorkerManagerImpl wmi = new WorkerManagerImpl(ids, WorkerState.IDLE);
		
		Collection<ServiceID> idle = wmi.getByState(WorkerState.IDLE);
		Collection<ServiceID> busy = wmi.getByState(WorkerState.BUSY);
		Collection<ServiceID> susp = wmi.getByState(WorkerState.SUSPECTED);
		
		wmi.executorSuspected(buildSID(0));
		
		ServiceID wid = wmi.allocateAvailableExecutor(build("1"));
		
		wmi.executorSuspected(wid);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(2, susp.size());
		Assert.assertEquals(8, idle.size());
		
		wmi.markAvailable(buildSID(0));
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(1, susp.size());
		Assert.assertEquals(9, idle.size());
		
		wmi.markAvailable(buildSID(0));
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(1, susp.size());
		Assert.assertEquals(9, idle.size());
		
		ServiceID wid2 = wmi.allocateAvailableExecutor(build("1"));
		Assert.assertEquals(1, busy.size());
		
		wmi.executorSuspected(wid2);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(2, susp.size());
		Assert.assertEquals(8, idle.size());
		
		wmi.markAvailable(wid);
		wmi.markAvailable(wid);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(1, susp.size());
		Assert.assertEquals(9, idle.size());
		
		wmi.markAvailable(wid2);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(10, idle.size());
	}

	private static CrawlID build(String i) {
		CrawlID.Builder builder = CrawlID.newBuilder();
		return builder.setId(i).build();
	}
}
