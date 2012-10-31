package br.ufmg.dcc.vod.spiderpig.ui;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import br.ufmg.dcc.vod.spiderpig.Crawler;
import br.ufmg.dcc.vod.spiderpig.CrawlerFactory;
import br.ufmg.dcc.vod.spiderpig.common.FileUtil;
import br.ufmg.dcc.vod.spiderpig.common.LoggerInitiator;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaverImpl;

public class MasterUP extends Command {

	private static final String LOG_FILE = "l";
	private static final String HOSTNAME = "h";
	private static final String PORT = "p";
	private static final String SERVER_FILE = "r";
	private static final String SAVE_FOLDER = "o";
	private static final String SEED_FILE = "s";
	private static final String WORKQUEUE_FOLDER = "w";
	
	@Override
	@SuppressWarnings("static-access")
	public Options getOptions() {
		Options opts = new Options();
		
		opts.addOption(OptionBuilder
				.withArgName("log-file")
				.hasArg()
				.isRequired()
				.withDescription("Log File")
				.create(LOG_FILE));

		opts.addOption(OptionBuilder
				.withArgName("hostname")
				.hasArg()
				.isRequired()
				.withDescription("A resolvable hostname for callbacks")
				.create(HOSTNAME));
		
		opts.addOption(OptionBuilder
				.withArgName("port")
				.hasArg()
				.isRequired()
				.withDescription("Port to bind socket")
				.create(PORT));

		opts.addOption(OptionBuilder
				.withArgName("server-file")
				.hasArg()
				.isRequired()
				.withDescription("Servers to use")
				.create(SERVER_FILE));
		
		opts.addOption(OptionBuilder
				.withArgName("save-folder")
				.hasArg()
				.isRequired()
				.withDescription("Data save folder")
				.create(SAVE_FOLDER));
		
		opts.addOption(OptionBuilder
				.withArgName("workqueue")
				.hasArg()
				.isRequired()
				.withDescription("Workqueue Folder")
				.create(WORKQUEUE_FOLDER));
		
		opts.addOption(OptionBuilder
				.withArgName("seed-file")
				.hasArg()
				.isRequired()
				.withDescription("Seed File")
				.create(SEED_FILE));
		
		return opts;
	}

	private static Set<InetSocketAddress> interpret(File serverFile) 
			throws Exception {
		LinkedHashSet<String> servers = FileUtil.readFileToSet(serverFile);
		Set<InetSocketAddress> rv = new HashSet<InetSocketAddress>();
		for (String line : servers) {
			String[] split = line.split(":");
			
			if (split.length != 2) {
				throw new Exception("Invalid servers files. " +
						"Each line must be host:port only!");
			}
			
			InetAddress name = InetAddress.getByName(split[0]);
			int port = Integer.parseInt(split[1]);
			
			rv.add(new InetSocketAddress(name.getHostAddress(), port));
		}
		
		return rv;
	}
	
	@Override
	public void exec(CommandLine cli) throws Exception {
		
		String hostname = cli.getOptionValue(HOSTNAME);
		int port = Integer.parseInt(cli.getOptionValue(PORT));
		
		File serverFile = new File(cli.getOptionValue(SERVER_FILE));
		File saveFolder = new File(cli.getOptionValue(SAVE_FOLDER));
		File workQueueFolder = new File(cli.getOptionValue(WORKQUEUE_FOLDER));
		File seedFile = new File(cli.getOptionValue(SEED_FILE));

		if (workQueueFolder.exists() && 
				(!workQueueFolder.isDirectory() || 
						workQueueFolder.list().length != 0)) {
			throw new Exception("work queue folder exists and is not empty");
		}
		
		LoggerInitiator.initiateLog(cli.getOptionValue(LOG_FILE));
		
		Set<InetSocketAddress> workerAddrs = interpret(serverFile);
		FileSaver saver = new FileSaverImpl(saveFolder.getAbsolutePath());
		Crawler crawler = 
				CrawlerFactory.createDistributedCrawler(hostname, port, 
					workerAddrs, workQueueFolder, saver);
		
		List<String> seed = FileUtil.readFileToList(seedFile);
		crawler.dispatch(seed);
		crawler.crawl();
	}

}
