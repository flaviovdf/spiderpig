package br.ufmg.dcc.vod.ncrawler.distributed.worker;

import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.MessageListener;
import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.NIOMessageSender;
import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Worker.CrawlRequest;

import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.MessageLite.Builder;

public class JobExecutorServer implements MessageListener<CrawlRequest> {

	private final JobExecutor executor;
	private final NIOMessageSender sender;

	public JobExecutorServer(JobExecutor executor) {
		this.executor = executor;
		this.sender = new NIOMessageSender();
	}

	@Override
	public void receiveMessage(CrawlRequest msg) {
		this.executor.crawl(msg.getId(),
				new InterestedProxy(msg.getCallBackHost(), 
									msg.getCallBackPort(),
									this.sender), 
				new FileSaverProxy(msg.getFileSaverHost(), 
							  	   msg.getFileSaverPort()));
	}

	@Override
	public ExtensionRegistryLite getRegistry() {
		return null;
	}

	@Override
	public Builder getNewBuilder() {
		return CrawlRequest.newBuilder();
	}
	
}