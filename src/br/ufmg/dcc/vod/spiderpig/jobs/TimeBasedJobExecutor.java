package br.ufmg.dcc.vod.spiderpig.jobs;

import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
import br.ufmg.dcc.vod.spiderpig.common.config.Configurable;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;

import com.google.common.collect.Sets;

public class TimeBasedJobExecutor implements Configurable, JobExecutor {

    public static final String BKOFF_TIME = "worker.job.backofftime";
    public static final String SLEEP_TIME = "worker.job.sleeptime";
    public static final String REQUESTER = "worker.requester";
    
    private Requester requester;
    private ThroughputManager manager;

    @Override
    public void crawl(CrawlID id, WorkerInterested interested) {
        CrawlResult result = manager.sleepAndPerform(id, requester);
        interested.crawlDone(result);
    }

    @Override
    public void configurate(Configuration configuration, 
            ConfigurableBuilder configurableBuilder) throws BuildException {
        long timeBetweenRequests = 
                configuration.getLong(SLEEP_TIME);
        long backOffTime = configuration.getLong(BKOFF_TIME);
        
        String requesterClass = configuration.getString(REQUESTER);
        this.requester = 
                configurableBuilder.build(requesterClass, 
                        configuration);
        this.manager = new ThroughputManager(timeBetweenRequests,
                backOffTime);
    }

    @Override
    public Set<String> getRequiredParameters() {
        return Sets.newHashSet(SLEEP_TIME, BKOFF_TIME, REQUESTER);
    }
}