package br.ufmg.dcc.vod.ncrawler.distributed.nio.service;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AbstractNIOService implements NIOService {

	private AsynchronousChannelGroup channelGroup;
	private KeepAliveThread keepAliveThread;
	private boolean hasKeepAliveThread;
	private boolean started;
	private final int nThreads;

	public AbstractNIOService() {
		this(-1);
	}
	
	public AbstractNIOService(int nThreads) {
		this.nThreads = nThreads;
		this.channelGroup = null;
		this.started = false;
	}
	
	@Override
	public boolean start(boolean keepAliveThread) {
		
		if (this.started) {
			return false;
		}
		
		try {
			ExecutorService executor = null;
			if (this.nThreads > 0) {
				executor = Executors.newFixedThreadPool(this.nThreads);
			} else {
				executor = Executors.newCachedThreadPool();
			}
			
			this.channelGroup =
					AsynchronousChannelGroup.withThreadPool(executor);
			startHooks();
			
			if (keepAliveThread) {
				this.hasKeepAliveThread = true;
				this.keepAliveThread = new KeepAliveThread();
				this.keepAliveThread.start();
			}
			this.started = true;
			return true;			
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Additional start operations
	 * @throws IOException 
	 */
	public abstract void startHooks() throws IOException;

	@Override
	public boolean shutdown() {
		if (!this.started) {
			return false;
		}
		
		try {
			this.channelGroup.shutdownNow();
			this.channelGroup.awaitTermination(1, TimeUnit.MINUTES);
			
			if (hasKeepAliveThread) {
				this.keepAliveThread.poison();
				this.keepAliveThread.join();
			}
			
			return true;
		} catch (InterruptedException | IOException e) {
			return false;
		}
	}

	/**
	 * Additional shutdown operations
	 * @throws IOException 
	 */
	public abstract void shutdownHooks() throws IOException;
	
	/**
	 * Get's the {@link AsynchronousChannelGroup} backing this service.
	 *  
	 * @return The channel group
	 */
	protected AsynchronousChannelGroup getChannelGroup() {
		return channelGroup;
	}
	
	protected AsynchronousServerSocketChannel newServerSocket() 
			throws IOException {
		return AsynchronousServerSocketChannel.open(getChannelGroup());
	}
	
	protected AsynchronousSocketChannel newSocket() throws IOException {
		return AsynchronousSocketChannel.open(getChannelGroup());
	}
}
