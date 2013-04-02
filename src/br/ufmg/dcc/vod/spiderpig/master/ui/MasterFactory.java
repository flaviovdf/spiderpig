package br.ufmg.dcc.vod.spiderpig.master.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.Crawler;
import br.ufmg.dcc.vod.spiderpig.CrawlerFactory;
import br.ufmg.dcc.vod.spiderpig.common.FileUtil;
import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaverImpl;
import br.ufmg.dcc.vod.spiderpig.master.walker.ConfigurableWalker;
import br.ufmg.dcc.vod.spiderpig.master.walker.ThreadSafeWalker;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class MasterFactory extends AbstractConfigurable<Crawler> {

	public static final String HOSTNAME = "service.hostname";
	public static final String PORT = "service.port";
	
	public static final String WORKERS = "master.workersfile";
	public static final String SAVE_FOLDER = "master.savefolder";
	public static final String QUEUE_FOLDER = "master.queuefolder";
	public static final String SEED_FILE = "master.seed";

	public static final String CACHE_ENABLED = "master.cache.enabled";
	public static final String CACHE_SIZE = "master.cache.maxsize";

	public static final String WALK_STRATEGY = "master.walkstrategy";
	
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
				SAVE_FOLDER, QUEUE_FOLDER, SEED_FILE, CACHE_ENABLED,
				CACHE_SIZE, WALK_STRATEGY));
	}

	@Override
	public Crawler realConfigurate(Configuration configuration)
			throws Exception {
		String hostname = configuration.getString(HOSTNAME);
		int port = configuration.getInt(PORT);
		
		File serverFile = new File(configuration.getString(WORKERS));
		File saveFolder = new File(configuration.getString(SAVE_FOLDER));
		File queueFolder = new File(configuration.getString(QUEUE_FOLDER));
		File seedFile = new File(configuration.getString(SEED_FILE));

		if (queueFolder.exists() && 
				(!queueFolder.isDirectory() || 
						queueFolder.list().length != 0)) {
			throw new IOException("work queue folder exists and is not empty");
		}
		
		//Load worker file
		Set<InetSocketAddress> workerAddrs = interpret(serverFile);
		
		//Create saver
		FileSaver saver = new FileSaverImpl(saveFolder.getAbsolutePath(), true);
		
		//Get seed
		List<String> seed = FileUtil.readFileToList(seedFile);

		//Create walker
		String masterClass = configuration.getString(WALK_STRATEGY);
		Constructor<?> constructor = Class.forName(masterClass)
				.getConstructor();
		ConfigurableWalker walker = 
				(ConfigurableWalker) constructor.newInstance();
		ThreadSafeWalker threadSafeWalker = new ThreadSafeWalker(walker);
		threadSafeWalker.configurate(configuration);
		
		//Cache
		boolean cacheEnabled = configuration.getBoolean(CACHE_ENABLED);
		int maxCacheSize = configuration.getInt(CACHE_SIZE);
		Cache<CrawlID, List<CrawlID>> cache = null;
		if (cacheEnabled) {
			//Approx LRU
			cache = CacheBuilder.newBuilder()
					.concurrencyLevel(workerAddrs.size())
					.maximumSize(maxCacheSize)
					.build();
		}
		
		//Finally, create crawler.
		Crawler crawler = 
				CrawlerFactory.createDistributedCrawler(hostname, port, 
					workerAddrs, queueFolder, saver, threadSafeWalker, cache);
		crawler.dispatch(seed);
		return crawler;
	}

}
