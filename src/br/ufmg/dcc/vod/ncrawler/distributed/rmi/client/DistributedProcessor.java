package br.ufmg.dcc.vod.ncrawler.distributed.rmi.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.distributed.rmi.server.EvaluatorFake;
import br.ufmg.dcc.vod.ncrawler.distributed.rmi.server.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.processor.AbstractProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class DistributedProcessor extends AbstractProcessor {

	private static final Logger LOG = Logger.getLogger(DistributedProcessor.class);
	
	@SuppressWarnings("unchecked")
	private EvaluatorFake toSend;
	
	private Scheduler scheduler;

	public <S, I, C> DistributedProcessor(long sleepTimePerExecution, QueueService service,
			Serializer<S> serializer, File queueFile, int queueSize, Set<ServerID> workers,
			Evaluator<I, C> evaluator, EvaluatorClient<I, C> client) 
			throws FileNotFoundException, IOException {
		super(workers.size(), sleepTimePerExecution, service, serializer, queueFile, queueSize, evaluator);

		this.scheduler = new Scheduler(workers);
		this.toSend = new EvaluatorFake<I, C>(client);
	}
	
	@Override
	public QueueProcessor<?> newQueueProcessor(int i) {
		return new DCrawlProcessor(i);
	}
	
	private class DCrawlProcessor implements QueueProcessor<CrawlJob> {
		private final int i;

		public DCrawlProcessor(int i) {
			this.i = i;
		}

		@Override
		public String getName() {
			return getClass().getName() + " " + i;
		}

		@Override
		public void process(CrawlJob t) {
			t.setEvaluator(toSend);
			
			/*
			 * Since there is one thread per server, and each thread releases the server
			 * after a crawl, there will always be an available executor.
			 */
			ServerID eid = scheduler.getNextAvailableExecutor();
			LOG.info("Dispatching job to " + eid);
			try {
				JobExecutor resolve = eid.resolve();
				resolve.collect(t);
			} catch (Exception e) {
				LOG.error("Unable to contact executor " + eid + ": ", e);
				eid.reset();
				t.setEvaluator(null);
				DistributedProcessor.this.dispatch(t);
			} finally {
				scheduler.releaseExecutor(eid);
			}
			
			try {
                if (sleepTimePerExecution > 0)
    				Thread.sleep(sleepTimePerExecution);
			} catch (InterruptedException e) {
			}
		}
	}
	
	private enum State {IDLE, BUSY}
	private class Scheduler {
		private Map<State, Collection<ServerID>> scheduleMap;
		
		public Scheduler(Set<ServerID> workers) {
			this.scheduleMap = new HashMap<State, Collection<ServerID>>();
			
			this.scheduleMap.put(State.IDLE, new LinkedList<ServerID>());
			this.scheduleMap.put(State.BUSY, new HashSet<ServerID>());
			
			for (ServerID e : workers) {
				this.scheduleMap.get(State.IDLE).add(e);
			}
		}
		
		public synchronized ServerID getNextAvailableExecutor() {
			ServerID eid = ((LinkedList<ServerID>)this.scheduleMap.get(State.IDLE)).removeFirst();
			this.scheduleMap.get(State.BUSY).add(eid);
			return eid;
		}
		
		public synchronized void releaseExecutor(ServerID eid) {
			this.scheduleMap.get(State.BUSY).remove(eid);
			this.scheduleMap.get(State.IDLE).add(eid);
		}
	}
}
