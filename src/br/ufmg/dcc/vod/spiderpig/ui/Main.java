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
import org.apache.commons.configuration.PropertiesConfiguration;

import br.ufmg.dcc.vod.spiderpig.common.Constants;
import br.ufmg.dcc.vod.spiderpig.common.LoggerInitiator;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;

public class Main {

    private static final Map<String, Class<? extends Command>> CMDS = 
            new HashMap<>();
            
    static {
        CMDS.put("worker_up", WorkerUP.class);
        CMDS.put("master_up", MasterUP.class);
        CMDS.put("worker_kill", WorkerKill.class);
        CMDS.put("worker_status", WorkerStatus.class);
    }
    
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
    
    @SuppressWarnings("unchecked")
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
        
        Class<? extends Command> commCls = CMDS.get(commandName);
        if (commCls == null) {
            try {
                commCls = (Class<? extends Command>) Class.forName(commandName);
            } catch (Exception e) {
                commCls = null;
            }
        }
        
        if (commCls == null) {
            System.err.println("Please choose a command from"
                    + " " + CMDS.keySet() + " or provide a class which"
                            + " extends the Command interface.");
            System.exit(EXIT_CODES.UNKNOWN_COMMAND);
        }
        
        Options opts = getOptions();

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
            ConfigurableBuilder configurableBuilder = new ConfigurableBuilder();
            GnuParser parser = new GnuParser();
            CommandLine cli = parser.parse(opts, shifted);
            
            String configFile = cli.getOptionValue("c");
            if (!new File(configFile).exists())
                throw new IOException("Unable to find configuration file " 
                            +configFile);
            
            PropertiesConfiguration props = 
                    new PropertiesConfiguration(configFile);
            props.setListDelimiter(Constants.LIST_DELIM);
            configurableBuilder.build(LoggerInitiator.class, props);
            
            Command command = configurableBuilder.build(commCls, props);
            command.configurate(props, configurableBuilder);
            command.exec();
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