package br.ufmg.dcc.vod.ncrawler.ui;

import java.net.InetAddress;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.ServerID;

public class CollectServerStatus {
	
	private static final String HOST = "h";
	private static final String PORT = "p";

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		Options opts = new Options();
		
		Option hostOpt = OptionBuilder.withArgName("host")
		.hasArg()
		.isRequired()
		.withDescription("Host to check")
		.create(PORT);
		
		Option portOpt = OptionBuilder.withArgName("port")
		.hasArg()
		.isRequired()
		.withDescription("Port to check")
		.create(HOST);
		
		opts.addOption(hostOpt);
		opts.addOption(portOpt);
		
		InetAddress host = null;
		int port = -1;
		
		try {
			GnuParser parser = new GnuParser();
			CommandLine cli = parser.parse(opts, args);
			
			host = InetAddress.getByName(cli.getOptionValue(HOST));
			port = Integer.parseInt(cli.getOptionValue(PORT));
		} catch (Exception e) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("java " + CollectServerStatus.class, opts);
			
			System.out.println();
			System.out.println();
			e.printStackTrace();
			System.exit(EXIT_CODES.ERROR);
		}
		
		ServerID sid = new ServerID(host.getHostAddress(), port);
		
		try {
			sid.resolve();
			System.out.println("Online!");
			System.exit(EXIT_CODES.OK);
		} catch (Exception e) {
			System.out.println("Offline!");
			System.exit(EXIT_CODES.ERROR);
		}
	}
}