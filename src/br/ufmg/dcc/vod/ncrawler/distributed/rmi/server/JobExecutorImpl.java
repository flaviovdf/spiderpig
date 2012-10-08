package br.ufmg.dcc.vod.ncrawler.distributed.rmi.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Constants;
import br.ufmg.dcc.vod.ncrawler.ui.EXIT_CODES;

/**
 * Each server exports a {@code JobExecutor} for collecting data. This class
 * will execute the a {@code CrawlJob}.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class JobExecutorImpl extends UnicastRemoteObject 
		implements JobExecutor {

	private static final Logger LOG = Logger.getLogger(JobExecutorImpl.class);
	
	private static final long serialVersionUID = Constants.SERIAL_UID;

	/**
	 * Creates a new executor to bind at the given port.
	 * 
	 * @param port Port to use
	 * @throws RemoteException If network errors occur
	 */
	protected JobExecutorImpl(int port) throws RemoteException {
		super(port);
	}

	@Override
	public void collect(CrawlJob<?, ?> crawlJob) {
		LOG.info("Received JOB - " + crawlJob.getID().toString());
		crawlJob.collect();
		LOG.info("Finished JOB - " + crawlJob.getID().toString());
	}

	@Override
	public void kill() throws RemoteException {
		LOG.info("Exiting!");
		System.exit(EXIT_CODES.OK);
	}
}
