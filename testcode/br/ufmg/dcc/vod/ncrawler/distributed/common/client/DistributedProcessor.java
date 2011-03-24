package br.ufmg.dcc.vod.ncrawler.distributed.common.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import br.edu.ufcg.lsd.commune.api.FailureNotification;
import br.edu.ufcg.lsd.commune.api.InvokeOnDeploy;
import br.edu.ufcg.lsd.commune.api.RecoveryNotification;
import br.edu.ufcg.lsd.commune.container.servicemanager.ServiceManager;
import br.edu.ufcg.lsd.commune.identification.DeploymentID;
import br.edu.ufcg.lsd.commune.identification.ServiceID;
import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.distributed.commune.client.EvaluatorClient;
import br.ufmg.dcc.vod.ncrawler.distributed.commune.client.FailureDetector;
import br.ufmg.dcc.vod.ncrawler.distributed.commune.server.EvaluatorProxy;
import br.ufmg.dcc.vod.ncrawler.distributed.commune.server.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.processor.AbstractProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class DistributedProcessor extends AbstractProcessor implements FailureDetector {

	@SuppressWarnings("unchecked")
	private EvaluatorProxy toSend;
	
	private ExecutorStateManager executorManager;

	private Set<ServiceID> workers;

	public <S, I, C> DistributedProcessor(long sleepTimePerExecution, QueueService service,
			Serializer<S> serializer, File queueFile, int queueSize, Set<ServiceID> workers,
			Evaluator<I, C> evaluator, EvaluatorClient<I, C> client) 
			throws FileNotFoundException, IOException {
		super(workers.size(), sleepTimePerExecution, service, serializer, queueFile, queueSize, evaluator);

		this.workers = workers;
		this.toSend = new EvaluatorProxy<I, C>(client);
		this.executorManager = new ExecutorStateManager(workers);
	}

	@Override
	@InvokeOnDeploy
	public void init(ServiceManager serviceManager) {
		for (ServiceID s : workers) {
			serviceManager.registerInterest(FailureDetector.NAME, s.toString(), JobExecutor.class);
		}
	}

	@FailureNotification
	public void executorDead(JobExecutor e, DeploymentID did) {
		this.executorManager.executorDied(did.getServiceID());
	}
	
	@RecoveryNotification
	public void executorAlive(JobExecutor e, DeploymentID did) {
		this.executorManager.executorUp(did.getServiceID(), e);
	}
	
	public void setWorkers(Set<ServiceID> workers) {
		// TODO Auto-generated method stub
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
			 * Blocks until available.
			 */
			JobExecutor je;
			try {
				je = executorManager.getNextAvailableExecutor();
				je.collect(t);
			} catch (InterruptedException e) {
			}
			
			try {
                if (sleepTimePerExecution > 0)
    				Thread.sleep(sleepTimePerExecution);
			} catch (InterruptedException e) {
			}
		}
	}
}
