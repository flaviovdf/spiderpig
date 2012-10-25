package br.ufmg.dcc.vod.ncrawler.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public abstract class Command {

	private static final Map<String, Command> CMDS = new HashMap<>();
	static {
		CMDS.put("worker_up", new WorkerUP());
		CMDS.put("master_up", new MasterUP());
		CMDS.put("threaded_up", new ThreadedUP());
	}
	
	public CommandLine parserOptions(String... args) throws ParseException {
		GnuParser parser = new GnuParser();
		return parser.parse(getOptions(), args);
	}
	
	public abstract Options getOptions();
	
	public abstract int exec(CommandLine cli) throws Exception;
	
	private static String[] shift(String[] args) {
		//Minor hack for shifting
		ArrayList<String> argsList = new ArrayList<>(args.length);
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
		String[] shifted = shift(args);
		Command command = CMDS.get(commandName);
		
		if (command == null) {
			System.err.println("Please choose a command " + CMDS.keySet());
			System.exit(EXIT_CODES.UNKNOWN_COMMAND);
		}
		
		Options opts = command.getOptions();
		try {
			GnuParser parser = new GnuParser();
			CommandLine cli = parser.parse(opts, shifted);
			System.exit(command.exec(cli));
		} catch (Exception e) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("java " + Command.class + " " + CMDS.keySet(), opts);
			e.printStackTrace();
			System.exit(EXIT_CODES.ERROR);
		}
	}

	
}
