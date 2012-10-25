package br.ufmg.dcc.vod.ncrawler.distributed.master;

import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.NIOMessageSender;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.jobs.WorkerInterested;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Worker.CrawlRequest;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Worker.CrawlRequest.Builder;

public class JobExecutorProxy implements JobExecutor {

	private final String receiverHost;
	private final int receiverPort;
	
	private final String callBackHost;
	private final int callBackPort;
	
	private final String fileSaverHost;
	private final int fileSaverPort;
	
	private final NIOMessageSender sender;

	public JobExecutorProxy(String receiverHost, int receiverPort, 
			String callBackHost, int callBackPort, 
			String fileSaverHost, int fileSaverPort) {
		this.receiverHost = receiverHost;
		this.receiverPort = receiverPort;
		this.callBackHost = callBackHost;
		this.callBackPort = callBackPort;
		this.fileSaverHost = fileSaverHost;
		this.fileSaverPort = fileSaverPort;
		this.sender = new NIOMessageSender();
	}
	
	@Override
	public void crawl(String id, WorkerInterested interested, FileSaver saver) {
		Builder builder = CrawlRequest.newBuilder();
		builder.setId(id);
		builder.setCallBackHost(callBackHost);
		builder.setCallBackPort(callBackPort);
		builder.setFileSaverHost(fileSaverHost);
		builder.setFileSaverPort(fileSaverPort);
		CrawlRequest msg = builder.build();
		sender.send(receiverHost, receiverPort, msg);
	}

}
