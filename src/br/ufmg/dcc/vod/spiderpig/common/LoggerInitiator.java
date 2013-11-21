package br.ufmg.dcc.vod.spiderpig.common;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
import br.ufmg.dcc.vod.spiderpig.common.config.Configurable;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;

/**
 * Configures Log4J Logger 
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class LoggerInitiator implements Configurable {

    public static final String LOG_FILE = "log.logfile";
    public static final String LOG_LEVEL = "log.level";
    
    public LoggerInitiator() {
        Logger.getRootLogger().setLevel(Level.OFF);
    }
    
    @Override
    public void configurate(Configuration configuration, 
            ConfigurableBuilder builder) throws BuildException {
        
        try {
            String logPath = configuration.getString(LOG_FILE);
            String logLevel = configuration.getString(LOG_LEVEL);
            Level level = Level.toLevel(logLevel);
            BasicConfigurator.configure(new DailyRollingFileAppender(
                    new PatternLayout("%d [%t] %-5p %c - %m%n"), logPath, 
                    "'.'yyyy-MM-dd"));
            Logger.getRootLogger().setLevel(level);
        } catch (IOException e) {
            throw new BuildException("Unable to create logger", e);
        }
    }

    @Override
    public Set<String> getRequiredParameters() {
        return new HashSet<String>(Arrays.asList(LOG_FILE, LOG_LEVEL));
    }
    
}
