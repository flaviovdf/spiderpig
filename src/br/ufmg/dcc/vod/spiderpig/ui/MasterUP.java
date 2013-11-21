package br.ufmg.dcc.vod.spiderpig.ui;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.Crawler;
import br.ufmg.dcc.vod.spiderpig.CrawlerFactory;
import br.ufmg.dcc.vod.spiderpig.common.FileUtil;
import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.filesaver.LevelDBSaver;
import br.ufmg.dcc.vod.spiderpig.master.walker.ConfigurableWalker;

public class MasterUP implements Command {

    public static final String HOSTNAME = "service.hostname";
    public static final String PORT = "service.port";
    
    public static final String WORKERS = "master.workersfile";
    public static final String SAVE_FILE = "master.savefile";
    public static final String QUEUE_FOLDER = "master.queuefolder";
    public static final String SEED_FILE = "master.seed";

    public static final String WALK_STRATEGY = "master.walkstrategy";
    
    public static final String FD_TIMEOUT = "master.fd.timeout_secs";
    public static final String FD_PING = "master.fd.ping_secs";
    
    private Crawler crawler;
    
    private Set<InetSocketAddress> interpret(File serverFile) 
            throws IOException {
        LinkedHashSet<String> servers = FileUtil.readFileToSet(serverFile);
        Set<InetSocketAddress> rv = new HashSet<InetSocketAddress>();
        for (String line : servers) {
            String[] split = line.split(":");
            
            if (split.length != 2) {
                throw new IOException("Invalid servers files. " +
                                      "Each line must be host:port only!");
            }
            
            InetAddress name = InetAddress.getByName(split[0]);
            int port = Integer.parseInt(split[1]);
            
            rv.add(new InetSocketAddress(name.getHostAddress(), port));
        }
        
        return rv;
    }
    
    @Override
    public Set<String> getRequiredParameters() {
        return new HashSet<>(Arrays.asList(HOSTNAME, PORT, WORKERS,
                SAVE_FILE, QUEUE_FOLDER, SEED_FILE, WALK_STRATEGY, 
                FD_TIMEOUT, FD_PING));
    }

    @Override
    public void configurate(Configuration configuration, 
            ConfigurableBuilder configurableBuilder) throws BuildException {
        String hostname = configuration.getString(HOSTNAME);
        int port = configuration.getInt(PORT);
        
        File serverFile = new File(configuration.getString(WORKERS));
        File saveFile = new File(configuration.getString(SAVE_FILE));
        File queueFolder = new File(configuration.getString(QUEUE_FOLDER));
        File seedFile = new File(configuration.getString(SEED_FILE));

        if (queueFolder.exists() && 
                (!queueFolder.isDirectory() || 
                        queueFolder.list().length != 0)) {
            throw new BuildException("work queue folder exists and is not "
                    + "empty", new IOException());
        }
        
        try {
            //Load worker file
            Set<InetSocketAddress> workerAddrs = interpret(serverFile);
            
            //Create saver
            FileSaver saver = 
                    new LevelDBSaver(saveFile.getAbsolutePath(), true);
            
            //Create walker
            String masterClass = configuration.getString(WALK_STRATEGY);
            ConfigurableWalker walker = 
                    configurableBuilder.build(masterClass, configuration);
            
            //FD options
            int timeout = configuration.getInt(FD_TIMEOUT);
            int ping = configuration.getInt(FD_PING);
            
            //Finally, create crawler.
            this.crawler = 
                    CrawlerFactory.createDistributedCrawler(hostname, port, 
                            workerAddrs, timeout, ping, queueFolder, saver, 
                            walker);
            crawler.addSeed(seedFile);
        } catch (IOException e) {
            throw new BuildException("io error! details: ", e);
        }
    }
    
    @Override
    public void exec() throws Exception {
        System.out.println("Starting Crawler");
        crawler.crawl();
        System.out.println("Crawl Done!!");
    }
}