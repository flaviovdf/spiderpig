package br.ufmg.dcc.vod.spiderpig;

import br.ufmg.dcc.vod.spiderpig.distributed.fd.FDClientActor;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaverActor;
import br.ufmg.dcc.vod.spiderpig.master.Master;
import br.ufmg.dcc.vod.spiderpig.master.ResultActor;
import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.stats.StatsActor;

public class DistributedCrawler extends ThreadedCrawler {

	private final ResultActor resultActor;
	private final FileSaverActor fileSaverActor;
	private final FDClientActor fd;

	public DistributedCrawler(ProcessorActor processorActor, 
			StatsActor statsActor, QueueService service, Master master, 
			ResultActor resultActor, FileSaverActor fileSaverActor, 
			FDClientActor fd, FileSaver saver, int numThreads) {
		super(processorActor, statsActor, service, master, saver, numThreads);
		this.resultActor = resultActor;
		this.fileSaverActor = fileSaverActor;
		this.fd = fd;
	}

	@Override
	public void crawl() {
		this.resultActor.startProcessors(numThreads);
		this.fileSaverActor.startProcessors(numThreads);
		this.fd.startProcessors(1);
		this.fd.startTimer();
		super.crawl();
		try {
			this.fd.stopTimer();
		} catch (InterruptedException e) {
		}
	}
}