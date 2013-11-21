package br.ufmg.dcc.vod.spiderpig.master;

import br.ufmg.dcc.vod.spiderpig.common.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.common.queue.serializer.MessageLiteSerializer;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;

public class ResultActor extends Actor<CrawlResult> 
        implements QueueProcessor<CrawlResult> {

    public static final String HANDLE = "ResultServer";
    private final WorkerInterested workerInterested;

    public ResultActor(WorkerInterested workerInterested) {
        super(HANDLE);
        this.workerInterested = workerInterested;
    }
    
    @Override
    public QueueProcessor<CrawlResult> getQueueProcessor() {
        return this;
    }

    @Override
    public MessageLiteSerializer<CrawlResult> newMsgSerializer() {
        return new MessageLiteSerializer<>(CrawlResult.newBuilder());
    }

    @Override
    public void process(CrawlResult msg) {
        this.workerInterested.crawlDone(msg);
    }
}