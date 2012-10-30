package br.ufmg.dcc.vod.ncrawler;

import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaverActor;
import br.ufmg.dcc.vod.ncrawler.master.Master;
import br.ufmg.dcc.vod.ncrawler.master.ResultActor;
import br.ufmg.dcc.vod.ncrawler.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.stats.StatsActor;

public class DistributedCrawler extends ThreadedCrawler {

	private final ResultActor resultActor;
	private final FileSaverActor fileSaverActor;

	public DistributedCrawler(ProcessorActor processorActor, 
			StatsActor statsActor, QueueService service, Master master, 
			ResultActor resultActor, FileSaverActor fileSaverActor, 
			FileSaver saver, int numThreads) {
		super(processorActor, statsActor, service, master, saver, numThreads);
		this.resultActor = resultActor;
		this.fileSaverActor = fileSaverActor;
	}

	@Override
	public void crawl() {
		this.resultActor.startProcessors(numThreads);
		this.fileSaverActor.startProcessors(numThreads);
		super.crawl();
	}
}