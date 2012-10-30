package br.ufmg.dcc.vod.spiderpig.common;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Configures Log4J Logger 
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class LoggerInitiator {

	/**
	 * Sets the basic configurations for our logger
	 * 
	 * @param logPath File which will be used for logging
	 * @throws IOException
	 */
	public static void initiateLog(String logPath) throws IOException {
		System.setProperty("log4j.disable", "DEBUG");
		BasicConfigurator.configure(new DailyRollingFileAppender(
				new PatternLayout("%d [%t] %-5p %c - %m%n"), logPath, 
				"'.'yyyy-MM-dd"));
		Logger.getRootLogger().setLevel(Level.INFO);
	}
	
}
