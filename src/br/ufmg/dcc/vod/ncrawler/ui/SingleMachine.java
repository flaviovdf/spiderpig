package br.ufmg.dcc.vod.ncrawler.ui;

import java.io.File;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import br.ufmg.dcc.vod.ncrawler.ThreadedCrawler;
import br.ufmg.dcc.vod.ncrawler.common.FileUtil;
import br.ufmg.dcc.vod.ncrawler.common.LoggerInitiator;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;
import br.ufmg.dcc.vod.ncrawler.tracker.ThreadSafeTrackerFactory;

public class SingleMachine {
	
	private static final String LOG_FILE = "l";
	private static final String SEED_FILE = "e";
	private static final String WORKQUEUE_FOLDER = "w";
	private static final String SAVE_FOLDER = "o";
	private static final String SLEEP_TIME = "s";
	private static final String CRAWLER = "c";
	private static final String NTHREADS = "t";
	
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		Options opts = new Options();
		Option crawlerToDispachOpt = OptionBuilder.withArgName("crawler-name")
		.hasArg()
		.isRequired()
		.withDescription("Crawler to Dispatch Option")
		.create(CRAWLER);
		
		Option nThreadsOpt = OptionBuilder.withArgName("int value")
		.hasArg()
		.isRequired()
		.withDescription("Number of Threads")
		.create(NTHREADS);
		
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
		
		opts.addOption(crawlerToDispachOpt);
		opts.addOption(nThreadsOpt);
		opts.addOption(sleepTimeOpt);
		opts.addOption(outFolderOpt);
		opts.addOption(workQueueFolderOpt);
		opts.addOption(seedFileOpt);
		opts.addOption(logFileOpt);
		
		try {
			GnuParser parser = new GnuParser();
			CommandLine cli = parser.parse(opts, args);
			
			int nThreads = Integer.parseInt(cli.getOptionValue(NTHREADS));
			long sleepTime = Long.parseLong(cli.getOptionValue(SLEEP_TIME)) * 1000;
			File saveFolder = new File(cli.getOptionValue(SAVE_FOLDER));
			File workQueueFolder = new File(cli.getOptionValue(WORKQUEUE_FOLDER));
			File seedFile = new File(cli.getOptionValue(SEED_FILE));
			
			String crawlerName = cli.getOptionValue(CRAWLER);
			EvaluatorFactory<?, ?, ?> crawlerFactory = CrawlerPool.get(crawlerName);

			if (crawlerFactory == null) {
				throw new Exception("unknown crawler");
			}
			
			if (saveFolder.exists() && (!saveFolder.isDirectory() || saveFolder.list().length != 0)) {
				throw new Exception("save folder exists and is not empty");
			}
			
			if (workQueueFolder.exists() && (!workQueueFolder.isDirectory() || workQueueFolder.list().length != 0)) {
				throw new Exception("work queue folder exists and is not empty");
			}
			
			LoggerInitiator.initiateLog(cli.getOptionValue(LOG_FILE));
			List<String> seeds = FileUtil.readFileToList(seedFile);
			
			crawlerFactory.initiate(nThreads, saveFolder, sleepTime, seeds);
			Evaluator<?, ?> evaluator = crawlerFactory.getEvaluator();
			evaluator.setTrackerFactory(new ThreadSafeTrackerFactory());
			
			Serializer<?> serializer = crawlerFactory.getSerializer();
			
			System.out.println("Initiating crawl");
			System.out.println("\t crawler: " + crawlerName);
			System.out.println("\t numer of threads: " + nThreads);
			System.out.println("\t sleep time: " + sleepTime);
			
			ThreadedCrawler tc = new ThreadedCrawler(nThreads, sleepTime, evaluator, workQueueFolder, serializer, 1024 * 1024 * 1024);
			tc.crawl();
			crawlerFactory.shutdown();
		} catch (Exception e) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("java " + SingleMachine.class, opts);
			
			System.out.println();
			System.out.println();
			e.printStackTrace();
			System.exit(EXIT_CODES.ERROR);
		}
	}
}