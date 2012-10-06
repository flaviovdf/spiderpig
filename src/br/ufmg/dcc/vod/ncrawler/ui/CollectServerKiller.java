package br.ufmg.dcc.vod.ncrawler.ui;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.ServerID;
import br.ufmg.dcc.vod.ncrawler.distributed.rmi.server.JobExecutor;

public class CollectServerKiller {

	private static final String HOST = "h";
	private static final String PORT = "p";

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		Options opts = new Options();
		
		Option hostOpt = OptionBuilder.withArgName("host")
		.hasArg()
		.isRequired()
		.withDescription("Host to check")
		.create(HOST);
		
		Option portOpt = OptionBuilder.withArgName("port")
		.hasArg()
		.isRequired()
		.withDescription("Port to check")
		.create(PORT);
		
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
			hf.printHelp("java " + CollectServerKiller.class, opts);
			
			System.out.println();
			System.out.println();
			e.printStackTrace();
			System.exit(EXIT_CODES.ERROR);
		}
		
		ServerID sid = new ServerID(host.getHostAddress(), port);
		JobExecutor resolve = null;
		
		try {
			resolve = sid.resolve();
		} catch (Exception e) {
			System.out.println("Already offline");
			System.exit(EXIT_CODES.STATE_UNCHANGED);
		}
		
		try {
			resolve.kill();
			System.out.println("Stopped!");
			System.exit(EXIT_CODES.OK);
		} catch (UnmarshalException e) {
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("Unable to stop!");
			System.exit(EXIT_CODES.ERROR);
		}
	}	
}