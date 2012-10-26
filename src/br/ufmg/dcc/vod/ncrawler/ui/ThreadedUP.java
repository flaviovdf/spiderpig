package br.ufmg.dcc.vod.ncrawler.ui;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import br.ufmg.dcc.vod.ncrawler.Crawler;
import br.ufmg.dcc.vod.ncrawler.CrawlerFactory;
import br.ufmg.dcc.vod.ncrawler.common.FileUtil;
import br.ufmg.dcc.vod.ncrawler.common.LoggerInitiator;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaverImpl;
import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;

public class ThreadedUP extends Command {
	
	private static final String LOG_FILE = "l";
	private static final String SEED_FILE = "s";
	private static final String WORKQUEUE_FOLDER = "w";
	private static final String SAVE_FOLDER = "o";
	private static final String SLEEP_TIME = "t";
	private static final String EXECUTOR_CLASS = "e";
	private static final String NTHREADS = "n";
	
	@SuppressWarnings("static-access")
	public Options getOptions() {
		Options opts = new Options();
		
		Option logFileOpt = OptionBuilder
				.withArgName("file")
				.hasArg()
				.isRequired()
				.withDescription("Log File")
				.create(LOG_FILE);
		
		Option executorOpt = OptionBuilder
				.withArgName("executor")
				.hasArg()
				.isRequired()
				.withDescription("Executor class to use")
				.create(EXECUTOR_CLASS);
		
		Option nThreadsOpt = OptionBuilder
				.withArgName("num-threads")
				.hasArg()
				.isRequired()
				.withDescription("Number of Threads")
				.create(NTHREADS);
		
		Option sleepTimeOpt = OptionBuilder
				.withArgName("sleep-time")
				.hasArg()
				.isRequired()
				.withDescription("Sleep time (seconds)")
				.create(SLEEP_TIME);
		
		Option outFolderOpt = OptionBuilder
				.withArgName("save-folder")
				.hasArg()
				.isRequired()
				.withDescription("Data save folder")
				.create(SAVE_FOLDER);
		
		Option workQueueFolderOpt = OptionBuilder
				.withArgName("work-folder")
				.hasArg()
				.isRequired()
				.withDescription("Workqueue Folder")
				.create(WORKQUEUE_FOLDER);
		
		Option seedFileOpt = OptionBuilder
				.withArgName("seed-file")
				.hasArg()
				.isRequired()
				.withDescription("Seed File")
				.create(SEED_FILE);
		
		opts.addOption(executorOpt);
		opts.addOption(nThreadsOpt);
		opts.addOption(sleepTimeOpt);
		opts.addOption(outFolderOpt);
		opts.addOption(workQueueFolderOpt);
		opts.addOption(seedFileOpt);
		opts.addOption(logFileOpt);
		return opts;
	}
	
	@Override
	public int exec(CommandLine cli) throws Exception {
		
		String cls = cli.getOptionValue(EXECUTOR_CLASS);
		int nThreads = Integer.parseInt(cli.getOptionValue(NTHREADS));
		long sleepTime = Long.parseLong(cli.getOptionValue(SLEEP_TIME)) * 1000;
		File saveFolder = new File(cli.getOptionValue(SAVE_FOLDER));
		File workQueueFolder = new File(cli.getOptionValue(WORKQUEUE_FOLDER));
		File seedFile = new File(cli.getOptionValue(SEED_FILE));
			
		if (saveFolder.exists() && (!saveFolder.isDirectory() || 
				saveFolder.list().length != 0)) {
			throw new Exception("save folder exists and is not empty");
		}
		
		if (workQueueFolder.exists() && (!workQueueFolder.isDirectory() || 
				workQueueFolder.list().length != 0)) {
			throw new Exception("work queue folder exists and is not empty");
		}
			
		LoggerInitiator.initiateLog(cli.getOptionValue(LOG_FILE));
		List<String> seeds = FileUtil.readFileToList(seedFile);
		
		FileSaver saver = new FileSaverImpl(saveFolder.getAbsolutePath());
		Constructor<?> constructor = Class.forName(cls)
				.getConstructor(Long.class);
		JobExecutor executor = (JobExecutor) constructor.newInstance(sleepTime);
		Crawler crawler = CrawlerFactory.createThreadedCrawler(nThreads, 
				workQueueFolder, saver, executor);
		
		crawler.dispatch(seeds);
		crawler.crawl();
		
		return EXIT_CODES.OK;
		
	}
}