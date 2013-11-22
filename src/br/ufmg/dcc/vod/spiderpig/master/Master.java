package br.ufmg.dcc.vod.spiderpig.master;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.common.FileLineIterable;
import br.ufmg.dcc.vod.spiderpig.common.StringUtils;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.FDListener;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaverActor;
import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
import br.ufmg.dcc.vod.spiderpig.master.processor.manager.WorkerManager;
import br.ufmg.dcc.vod.spiderpig.master.walker.Walker;
import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.Payload;
import br.ufmg.dcc.vod.spiderpig.worker.WorkerActor;

public class Master implements WorkerInterested, FDListener {

    private static final int BUFFER_SIZE = 5 * 1024 * 1024;

    private static final Logger LOG = Logger.getLogger(Master.class);
    
    private final Walker walker;
    private final WorkerManager workerManager;
    private final FileSaverActor fileSaverActor;
    private final StopCondition stopCondition;
    private ProcessorActor processorActor;
    
    public Master(Walker walker, WorkerManager workerManager, 
            FileSaverActor fileSaverActor) {
        this.walker = walker;
        this.workerManager = workerManager;
        this.fileSaverActor = fileSaverActor;
        this.stopCondition = walker.getStopCondition();
    }
    
    public void setSeeds(Iterable<String> seeds) {
        Iterable<CrawlID> seedIds = StringUtils.toCrawlIdIterable(seeds);
        Iterable<CrawlID> seedDispatch = this.walker.filterSeeds(seedIds);
        for (CrawlID id : seedDispatch) {
            dispatch(id);
            this.stopCondition.dispatched();
        }
    }
    
    public void setSeeds(File seedFile) throws IOException {
        setSeeds(new FileLineIterable(seedFile, BUFFER_SIZE));
    }
    
    @Override
    public void crawlDone(CrawlResult crawlResult) {
        CrawlID crawled = crawlResult.getId();
        
        if (!crawlResult.getIsError()) {
            LOG.info("Received result for " + crawled);
            List<Payload> payLoadList = crawlResult.getPayLoadList();
            for(Payload payload : payLoadList)
                this.fileSaverActor.dispatch(payload);
            
            this.workerManager.freeExecutor(crawled);
            Iterable<CrawlID> toWalk = 
                    walker.walk(crawled, crawlResult.getToQueueList());
            for (CrawlID newId : toWalk) {
                dispatch(newId);
                this.stopCondition.dispatched();
            }
            this.stopCondition.resultReceived();
        } else {
            LOG.info("Received error for " + crawled + " cause = " +
                    crawlResult.getErrorMessage());
            
            this.workerManager.freeExecutor(crawled);
            this.walker.errorReceived(crawled);
            this.stopCondition.errorReceived();
        }
    }

    public StopCondition getStopCondition() {
        return this.walker.getStopCondition();
    }

    @Override
    public void isUp(ServiceID serviceID) {
        ServiceID workerID = ServiceID.newBuilder(serviceID)
                .setHandle(WorkerActor.HANDLE).build();
        LOG.info("Worker up " + workerID);
        this.workerManager.markAvailable(workerID);
    }

    @Override
    public void isSuspected(ServiceID serviceID) {
        ServiceID workerID = ServiceID.newBuilder(serviceID)
                .setHandle(WorkerActor.HANDLE).build();
        CrawlID cid = this.workerManager.executorSuspected(workerID);
        LOG.info("Worker suspected " + workerID);
        
        if (cid != null)
            dispatch(cid);
    }
    
    private void dispatch(CrawlID crawlID) {
        this.processorActor.dispatch(crawlID);
    }

    public void setProcessorActor(ProcessorActor processorActor) {
        this.processorActor = processorActor;
    }
}