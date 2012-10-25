package br.ufmg.dcc.vod.ncrawler.master.processor.manager;

import java.util.Collection;
import java.util.HashSet;

import junit.framework.Assert;

import org.junit.Test;

import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.master.processor.manager.WorkerManagerImpl.WorkerState;

public class WorkerManagerImplTest {

	class TestID implements WorkerID {

		private int id;

		public TestID(int id) {
			this.id = id;
		}
		
		@Override
		public JobExecutor resolve() {
			return null;
		}
		
		@Override
		public int hashCode() {
			return id;
		}
		
		@Override
		public boolean equals(Object obj) {
			return id == ((TestID) obj).id;
		}
	}
	
	private Collection<WorkerID> createIDs(int n) {
		HashSet<WorkerID> set = new HashSet<>();
		for (int i = 0; i < n; i++)
			set.add(new TestID(i));
		return set;
	}
	
	@Test
	public void testCreation() {
		Collection<WorkerID> ids = createIDs(10);
		WorkerManagerImpl wmi = new WorkerManagerImpl(ids);
		Collection<WorkerID> idle = wmi.getByState(WorkerState.IDLE);
		Collection<WorkerID> busy = wmi.getByState(WorkerState.BUSY);
		Collection<WorkerID> susp = wmi.getByState(WorkerState.SUSPECTED);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(10, idle.size());
		Assert.assertTrue(idle.containsAll(ids));
	}
	
	@Test
	public void testAllocateAvailableExecutor() throws InterruptedException {
		Collection<WorkerID> ids = createIDs(10);
		
		WorkerManagerImpl wmi = new WorkerManagerImpl(ids);
		
		Collection<WorkerID> idle = wmi.getByState(WorkerState.IDLE);
		Collection<WorkerID> busy = wmi.getByState(WorkerState.BUSY);
		Collection<WorkerID> susp = wmi.getByState(WorkerState.SUSPECTED);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(10, idle.size());
		
		WorkerID wid = wmi.allocateAvailableExecutor("1");
		
		Assert.assertEquals(1, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(9, idle.size());
		Assert.assertTrue(busy.contains(wid));
		
		Assert.assertNull(wmi.allocateAvailableExecutor("1"));
		
		WorkerID wid2 = wmi.allocateAvailableExecutor("2");
		
		Assert.assertEquals(2, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(8, idle.size());
		Assert.assertTrue(busy.contains(wid));
		Assert.assertTrue(busy.contains(wid2));
	}

	private class Allocator extends Thread {
		private final WorkerManager wm;
		private final String id;

		public Allocator(WorkerManager wm, String id) {
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
		Collection<WorkerID> ids = createIDs(10);
		
		WorkerManagerImpl wmi = new WorkerManagerImpl(ids);
		
		Collection<WorkerID> idle = wmi.getByState(WorkerState.IDLE);
		Collection<WorkerID> busy = wmi.getByState(WorkerState.BUSY);
		Collection<WorkerID> susp = wmi.getByState(WorkerState.SUSPECTED);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(10, idle.size());
		
		wmi.allocateAvailableExecutor("1");
		wmi.allocateAvailableExecutor("2");
		
		Assert.assertEquals(2, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(8, idle.size());
		
		Assert.assertTrue(wmi.freeExecutor("2"));
		
		Assert.assertEquals(1, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(9, idle.size());
		
		Assert.assertTrue(wmi.freeExecutor("1"));
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(10, idle.size());
		
		Assert.assertFalse(wmi.freeExecutor("1"));
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(10, idle.size());
		Assert.assertTrue(idle.containsAll(ids));
	}

	@Test
	public void testThreadSafety1() throws InterruptedException {
		Collection<WorkerID> ids = createIDs(2);

		WorkerManagerImpl wmi = new WorkerManagerImpl(ids);
		
		wmi.allocateAvailableExecutor("1");
		wmi.allocateAvailableExecutor("2");
		
		Collection<WorkerID> busy = wmi.getByState(WorkerState.BUSY);
		Assert.assertEquals(2, busy.size());
		
		Allocator allocator = new Allocator(wmi, "3");
		allocator.start();
		
		while(allocator.getState() != Thread.State.WAITING);
		
		wmi.freeExecutor("1");
		
		Assert.assertEquals(1, busy.size());
		
		allocator.join();
		
		Assert.assertEquals(2, busy.size());
	}
	
	@Test
	public void testExecutorSuspected() throws InterruptedException {
		Collection<WorkerID> ids = createIDs(10);
		
		WorkerManagerImpl wmi = new WorkerManagerImpl(ids);
		
		Collection<WorkerID> idle = wmi.getByState(WorkerState.IDLE);
		Collection<WorkerID> busy = wmi.getByState(WorkerState.BUSY);
		Collection<WorkerID> susp = wmi.getByState(WorkerState.SUSPECTED);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(0, susp.size());
		Assert.assertEquals(10, idle.size());
		
		wmi.executorSuspected(new TestID(0));
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(1, susp.size());
		Assert.assertEquals(9, idle.size());
		
		Assert.assertTrue(susp.contains(new TestID(0)));
		Assert.assertFalse(idle.contains(new TestID(0)));
		
		WorkerID wid = wmi.allocateAvailableExecutor("1");
		
		Assert.assertEquals(1, busy.size());
		Assert.assertEquals(1, susp.size());
		Assert.assertEquals(8, idle.size());
		
		Assert.assertTrue(susp.contains(new TestID(0)));
		Assert.assertFalse(idle.contains(new TestID(0)));
		
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
		Collection<WorkerID> ids = createIDs(10);
		
		WorkerManagerImpl wmi = new WorkerManagerImpl(ids);
		
		Collection<WorkerID> idle = wmi.getByState(WorkerState.IDLE);
		Collection<WorkerID> busy = wmi.getByState(WorkerState.BUSY);
		Collection<WorkerID> susp = wmi.getByState(WorkerState.SUSPECTED);
		
		wmi.executorSuspected(new TestID(0));
		
		WorkerID wid = wmi.allocateAvailableExecutor("1");
		
		wmi.executorSuspected(wid);
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(2, susp.size());
		Assert.assertEquals(8, idle.size());
		
		wmi.markAvailable(new TestID(0));
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(1, susp.size());
		Assert.assertEquals(9, idle.size());
		
		wmi.markAvailable(new TestID(0));
		
		Assert.assertEquals(0, busy.size());
		Assert.assertEquals(1, susp.size());
		Assert.assertEquals(9, idle.size());
		
		WorkerID wid2 = wmi.allocateAvailableExecutor("1");
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

}
