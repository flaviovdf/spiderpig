package br.ufmg.dcc.vod.ncrawler.distributed.nio.service;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * This interface represents services which make use of Java's nio, more 
 * specifically, {@link AsynchronousSocketChannel} and 
 * {@link AsynchronousServerSocketChannel}, for communication.
 *  
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface NIOService {

	/**
	 * Starts this service
	 * 
	 * @param keepAliveThread If true, a thread will be started to keep this
	 *                        service alive. This is useful when only the 
	 *                        service composes an application.
	 * @return true is service was started, false otherwise.
	 */
	public boolean start(boolean keepAliveThread);
	
	/**
	 * Stops the service.
	 * 
	 * @return true is service was stopped, false otherwise.
	 */
	public boolean shutdown();
	
}
