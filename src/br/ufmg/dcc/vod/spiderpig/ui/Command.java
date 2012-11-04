package br.ufmg.dcc.vod.spiderpig.ui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import br.ufmg.dcc.vod.spiderpig.common.Constants;

public abstract class Command {

	private static final Map<String, Command> CMDS = new HashMap<>();
	static {
		CMDS.put("worker_up", new WorkerUP());
		CMDS.put("master_up", new MasterUP());
		CMDS.put("worker_kill", new WorkerKill());
		CMDS.put("worker_status", new WorkerKill());
	}
	
	public abstract void exec(Configuration cli) throws Exception;
	
	@SuppressWarnings("static-access")
	private static Options getOptions() {
		Options opts = new Options();
		opts.addOption(new Option("help", "print this message"));
		opts.addOption(new Option("version", "print version info"));
		
		opts.addOption(OptionBuilder
				.withArgName("config-file")
				.hasArg()
				.isRequired()
				.withDescription("configuration file")
				.create("c"));

		opts.addOption(OptionBuilder
				.withArgName("jar-file")
				.hasArg()
				.withDescription("jar file to load additional resources")
				.create("j"));
		
		return opts;
	}
	
	private static String[] shift(String[] args) {
		//Minor hack for shifting
		List<String> argsList = new LinkedList<>(Arrays.asList(args));
		argsList.remove(0);
		String[] shiftedArgs = new String[args.length - 1];
		shiftedArgs = argsList.toArray(shiftedArgs);
		return shiftedArgs;
	}
	
	public static void main(String[] args) {

		if (args == null || args.length == 0) {
			System.err.println("Please choose a command " + CMDS.keySet());
			System.exit(EXIT_CODES.UNKNOWN_COMMAND);
		}
		
		String commandName = args[0];
		String[] shifted;
		if (args.length > 1)
			shifted = shift(args);
		else
			shifted = new String[0];
		Command command = CMDS.get(commandName);
		
		if (command == null) {
			System.err.println("Please choose a command " + CMDS.keySet());
			System.exit(EXIT_CODES.UNKNOWN_COMMAND);
		}
		
		Options opts = getOptions();
		opts.addOption(new Option("help", "print this message"));
		opts.addOption(new Option("version", "print version info"));

		for (String opt : shifted)
			if (opt.equals("help") || opt.equals("-help")) {
				HelpFormatter hf = new HelpFormatter();
				hf.printHelp(Constants.CMD_LINE + " " + CMDS.keySet() + 
						System.lineSeparator() + " help for command " +
						commandName, opts);
				System.exit(EXIT_CODES.OK);
			} else if (opt.equals("version") || opt.equals("-version")) {
				System.out.println(Constants.VERSION_MAJOR + "." +
						Constants.VERSION_MINOR);
				System.exit(EXIT_CODES.OK);
			}
				
			
		try {
			GnuParser parser = new GnuParser();
			CommandLine cli = parser.parse(opts, shifted);
			
			String configFile = cli.getOptionValue("c");
			if (!new File(configFile).exists())
				throw new IOException("Unable to find configuration file " 
							+configFile);
			
			command.exec(new PropertiesConfiguration(configFile));
		} catch (Exception e) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp(Constants.CMD_LINE + " " + CMDS.keySet(), opts);
			
			if (!(e instanceof MissingOptionException))
				e.printStackTrace();
			else
				System.err.println(e.getMessage());
			
			System.exit(EXIT_CODES.ERROR);
		}
	}	
}