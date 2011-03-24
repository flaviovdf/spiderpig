package br.ufmg.dcc.vod.ncrawler.ui;

import java.io.File;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import br.ufmg.dcc.vod.ncrawler.DistributedCrawler;
import br.ufmg.dcc.vod.ncrawler.common.FileUtil;
import br.ufmg.dcc.vod.ncrawler.common.LoggerInitiator;
import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.EvaluatorClientFactory;
import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.EvaluatorClientImpl;
import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.ServerID;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;
import br.ufmg.dcc.vod.ncrawler.tracker.ThreadSafeTrackerFactory;

public class CollectClient {

	private static final String LOG_FILE = "l";
	private static final String SEED_FILE = "e";
	private static final String WORKQUEUE_FOLDER = "w";
	private static final String SAVE_FOLDER = "o";
	private static final String SLEEP_TIME = "s";
	private static final String CRAWLER = "c";
	private static final String SERVER_FILE = "r";
	private static final String PORT = "p";
	private static final String IGNORE = "i";

	@SuppressWarnings({ "static-access", "unchecked" })
	public static void main(String[] args) {
		Options opts = new Options();
		
		Option portOpt = OptionBuilder.withArgName("port")
		.hasArg()
		.isRequired()
		.withDescription("Port to Bind")
		.create(PORT);
		
		Option crawlerToDispachOpt = OptionBuilder.withArgName("crawler-name")
		.hasArg()
		.isRequired()
		.withDescription("Crawler to Dispatch Option")
		.create(CRAWLER);
		
		Option serverFileOpts = OptionBuilder.withArgName("file")
		.hasArg()
		.isRequired()
		.withDescription("Servers to use")
		.create(SERVER_FILE);
		
		Option sleepTimeOpt = OptionBuilder.withArgName("long value")
		.hasArg()
		.isRequired()
		.withDescription("Sleep time (seconds)")
		.create(SLEEP_TIME);
		
		Option outFolderOpt = OptionBuilder.withArgName("folder")
		.hasArg()
		.isRequired()
		.withDescription("Data save folder")
		.create(SAVE_FOLDER);
		
		Option workQueueFolderOpt = OptionBuilder.withArgName("folder")
		.hasArg()
		.isRequired()
		.withDescription("Workqueue Folder")
		.create(WORKQUEUE_FOLDER);
		
		Option seedFileOpt = OptionBuilder.withArgName("file")
		.hasArg()
		.isRequired()
		.withDescription("Seed File")
		.create(SEED_FILE);
		
		Option logFileOpt = OptionBuilder.withArgName("file")
		.hasArg()
		.isRequired()
		.withDescription("Log File")
		.create(LOG_FILE);
		
		Option ignoreOpt = OptionBuilder.withArgName("ignore")
		.hasArg()
		.isRequired(false)
		.withDescription("IDs to ignore")
		.create(IGNORE);
		
		opts.addOption(crawlerToDispachOpt);
		opts.addOption(serverFileOpts);
		opts.addOption(portOpt);
		opts.addOption(sleepTimeOpt);
		opts.addOption(outFolderOpt);
		opts.addOption(workQueueFolderOpt);
		opts.addOption(seedFileOpt);
		opts.addOption(logFileOpt);
		opts.addOption(ignoreOpt);
		
		try {
			GnuParser parser = new GnuParser();
			CommandLine cli = parser.parse(opts, args);
			
			int port = Integer.parseInt(cli.getOptionValue(PORT));
			File serverFile = new File(cli.getOptionValue(SERVER_FILE));
			long sleepTime = Long.parseLong(cli.getOptionValue(SLEEP_TIME)) * 1000;
			File saveFolder = new File(cli.getOptionValue(SAVE_FOLDER));
			File workQueueFolder = new File(cli.getOptionValue(WORKQUEUE_FOLDER));
			File seedFile = new File(cli.getOptionValue(SEED_FILE));
			
			String crawlerName = cli.getOptionValue(CRAWLER);
			EvaluatorFactory<?, ?, ?> crawlerFactory = CrawlerPool.get(crawlerName);

			if (crawlerFactory == null) {
				throw new Exception("unknown crawler");
			}
			
			//Testing ignore
			Set ignoreIDs = null;
			boolean ignore = cli.hasOption(IGNORE);
			if (!ignore) {
				if (saveFolder.exists() && (!saveFolder.isDirectory() || saveFolder.list().length != 0)) {
					throw new Exception("save folder exists and is not empty");
				}
			} else {
				File ignoreFile = new File(cli.getOptionValue(IGNORE));
				ignoreIDs = FileUtil.readFileToSet(ignoreFile);
			}
			
			if (workQueueFolder.exists() && (!workQueueFolder.isDirectory() || workQueueFolder.list().length != 0)) {
				throw new Exception("work queue folder exists and is not empty");
			}
			
			EvaluatorClientImpl<?, ?> eci = null;
			try {
				EvaluatorClientFactory<?, ?> ecf = new EvaluatorClientFactory(port);
				eci = ecf.createAndBind();
			} catch (Exception e) {
				System.out.println("Already UP!");
				System.exit(EXIT_CODES.STATE_UNCHANGED);
			}
			
			LoggerInitiator.initiateLog(cli.getOptionValue(LOG_FILE));
			List<String> seeds = FileUtil.readFileToList(seedFile);
			LinkedHashSet<String> servers = FileUtil.readFileToSet(serverFile);
			Set<ServerID> serverIDs = interpret(servers);
			
			crawlerFactory.initiate(servers.size(), saveFolder, sleepTime, seeds);
			
			Evaluator<?, ?> evaluator = crawlerFactory.getEvaluator();
			evaluator.setTrackerFactory(new ThreadSafeTrackerFactory());
			
			if (ignoreIDs != null)
				evaluator.ignore(ignoreIDs);
			
			Serializer<?> serializer = crawlerFactory.getSerializer();
			
			System.out.println("Initiating crawl");
			System.out.println("\t crawler: " + crawlerName);
			System.out.println("\t numer of servers: " + servers.size());
			System.out.println("\t sleep time: " + sleepTime);
			
			DistributedCrawler dc = new DistributedCrawler(serverIDs, sleepTime, eci, evaluator, workQueueFolder, serializer, 1024 * 1024 * 1024);
			dc.crawl();
			crawlerFactory.shutdown();
		} catch (Exception e) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("java " + CollectClient.class, opts);
			
			System.out.println();
			System.out.println();
			e.printStackTrace();
			System.exit(EXIT_CODES.ERROR);
		}
	}

	private static Set<ServerID> interpret(LinkedHashSet<String> servers) throws Exception {
		Set<ServerID> rv = new HashSet<ServerID>();
		for (String line : servers) {
			String[] split = line.split(":");
			
			if (split.length != 2) {
				throw new Exception("Invalid servers files. Each line must be host:port only!");
			}
			
			try {
				InetAddress name = InetAddress.getByName(split[0]);
				int port = Integer.parseInt(split[1]);
				
				rv.add(new ServerID(name.getHostAddress(), port));
			} catch (Exception e) {
				throw new Exception("Invalid servers files. Each line must be host:port only!");
			}
		}
		
		return rv;
	}
}
