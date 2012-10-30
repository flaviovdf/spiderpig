package br.ufmg.dcc.vod.ncrawler.distributed.master;

import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.RemoteMessageSender;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.jobs.WorkerInterested;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Worker.CrawlRequest;

public class JobExecutorProxy implements JobExecutor {

	private final ServiceID workerID;
	private final ServiceID callBackID;
	private final ServiceID fileSaverID;
	
	private final RemoteMessageSender sender;

	public JobExecutorProxy(ServiceID workerID, ServiceID callBackID,
			ServiceID fileSaverID, RemoteMessageSender sender) {
		this.workerID = workerID;
		this.callBackID = callBackID;
		this.fileSaverID = fileSaverID;
		this.sender = sender;
	}
	
	@Override
	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver) {
		CrawlRequest.Builder builder = CrawlRequest.newBuilder();
		builder.setId(id);
		
		builder.setCallBackID(callBackID);
		builder.setFileSaverID(fileSaverID);
		
		CrawlRequest msg = builder.build();
		sender.send(workerID, msg);
	}

}
