package br.ufmg.dcc.vod.spiderpig.ui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import br.ufmg.dcc.vod.spiderpig.common.Constants;

public abstract class Command {

	private static final Map<String, Command> CMDS = new HashMap<>();
	static {
		CMDS.put("worker_up", new WorkerUP());
		CMDS.put("master_up", new MasterUP());
		CMDS.put("threaded_up", new ThreadedUP());
	}
	
	public abstract Options getOptions();
	
	public abstract void exec(CommandLine cli) throws Exception;
	
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
		
		Options opts = command.getOptions();
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
			command.exec(cli);
		} catch (Exception e) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp(Constants.CMD_LINE + " " + CMDS.keySet(), opts);
			e.printStackTrace();
			System.exit(EXIT_CODES.ERROR);
		}
	}	
}