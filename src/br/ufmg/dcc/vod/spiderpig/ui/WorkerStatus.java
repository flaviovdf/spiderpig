package br.ufmg.dcc.vod.spiderpig.ui;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.ServiceIDUtils;
import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.common.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.FDClientActor;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.FDListener;
import br.ufmg.dcc.vod.spiderpig.common.distributed.fd.FDServerActor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueService;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

import com.google.common.collect.Sets;

public class WorkerStatus implements Command {

    public static final String HOSTNAME = "control.hostname";
    public static final String PORT = "control.port";

    public static final String WORKER_HOSTNAME = "service.hostname";
    public static final String WORKER_PORT = "service.port";
    
    /*
     * Timeout to declare the service as offline 
     */
    private static final int TIMEOUT = 5;
    private static final int PING = 1;
    
    private String hostname;
    private int port;
    
    private String workerHostname;
    private int workerPort;
    
    @Override
    public void configurate(Configuration configuration,
            ConfigurableBuilder builder) throws BuildException {
        this.hostname = configuration.getString(HOSTNAME);
        this.port = configuration.getInt(PORT);
        this.workerHostname = configuration.getString(WORKER_HOSTNAME);
        this.workerPort = configuration.getInt(WORKER_PORT);
    }

    @Override
    public Set<String> getRequiredParameters() {
        return Sets.newHashSet(HOSTNAME, PORT, WORKER_HOSTNAME, WORKER_PORT);
    }
    
    @Override
    public void exec() throws Exception {
        RemoteMessageSender sender = new RemoteMessageSender();
        QueueService service = new QueueService(hostname, port);
        
        FDListener listener = new FDStatusListener();
        FDClientActor actor = new FDClientActor(TIMEOUT, PING, 
                TimeUnit.SECONDS, listener, sender);
        ServiceID workerID = ServiceIDUtils.toResolvedServiceID(workerHostname, 
                workerPort, FDServerActor.HANDLE);

        actor.withSimpleQueue(service).startProcessors(1);
        actor.watch(workerID);
        actor.startTimer();
    }
    
    private class FDStatusListener implements FDListener {

        @Override
        public void isUp(ServiceID serviceID) {
            System.out.println("Worker UP");
            System.exit(EXIT_CODES.OK);
        }
        
        @Override
        public void isSuspected(ServiceID serviceID) {
            System.out.println("Worker Down");
            System.exit(EXIT_CODES.OK);
        }
    }
}
