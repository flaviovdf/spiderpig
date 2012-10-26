package br.ufmg.dcc.vod.ncrawler;

import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.NIOServer;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.master.Master;
import br.ufmg.dcc.vod.ncrawler.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Payload.UploadMessage;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Worker.BaseResult;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.stats.StatsActor;

public class DistributedCrawler extends ThreadedCrawler {

	private final NIOServer<BaseResult> resultServer;
	private final NIOServer<UploadMessage> fileServer;

	public DistributedCrawler(ProcessorActor processorActor, 
			StatsActor statsActor, QueueService service, Master master, 
			NIOServer<BaseResult> resultServer, 
			NIOServer<UploadMessage> fileServer, FileSaver saver) {
		super(processorActor, statsActor, service, master, saver);
		this.resultServer = resultServer;
		this.fileServer = fileServer;
	}

	@Override
	public void crawl() {
		this.resultServer.start(true);
		this.fileServer.start(true);
		super.crawl();
		this.fileServer.shutdown();
		this.resultServer.shutdown();
	}

}
