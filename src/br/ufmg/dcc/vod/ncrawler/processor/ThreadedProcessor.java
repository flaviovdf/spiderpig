package br.ufmg.dcc.vod.ncrawler.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class ThreadedProcessor<I, C> extends AbstractThreadedProcessor<I, C> {
	
	public ThreadedProcessor(int nThreads, long sleepTimePerExecution, 
			QueueService service, Serializer<I> serializer, File queueFile, 
			int queueSize, Evaluator<I, C> eval) 
					throws FileNotFoundException, IOException {
		super(nThreads, sleepTimePerExecution, service, serializer, queueFile, 
				queueSize, eval);
	}
	
	@Override
	public QueueProcessor<?> newQueueProcessor(int i) {
		return new CrawlProcessor(i);
	}
	
	private class CrawlProcessor implements QueueProcessor<CrawlJob<I, C>> {
		private final int i;

		public CrawlProcessor(int i) {
			this.i = i;
		}

		@Override
		public String getName() {
			return getClass().getName() + " " + i;
		}

		@Override
		public void process(CrawlJob<I, C> t) {
			t.setEvaluator(eval);
			t.collect();
			
			try {
                if (sleepTimePerExecution > 0)
    				Thread.sleep(sleepTimePerExecution);
			} catch (InterruptedException e) {
			}
		}
	}
}
