package br.ufmg.dcc.vod.spiderpig.queue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.queue.common.ProtocolBufferUtils;
import br.ufmg.dcc.vod.spiderpig.queue.serializer.MessageLiteSerializer;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

/**
 * This interface represents services which make use of Java's nio, more 
 * specifically, {@link AsynchronousSocketChannel} and 
 * {@link AsynchronousServerSocketChannel}, for communication. All received
 * messages are queued in a {@link QueueService}
 *  
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class NIOServer 
		implements CompletionHandler<AsynchronousSocketChannel, Integer> {

	private static final Logger LOG = Logger.getLogger(NIOServer.class);
	
	private final AsynchronousChannelGroup channelGroup;
	private final String hostname;
	private final int port;
	private final AtomicInteger atomicInteger;
	private final CompletionHandler<Integer, ByteBuffer> readHandler;
	private final QueueService queueService;
	
	private AsynchronousServerSocketChannel serverSocket;

	public NIOServer(ExecutorService executor, QueueService queueService, 
			String hostname, int port) throws IOException {
		this.channelGroup = AsynchronousChannelGroup.withThreadPool(executor);
		this.hostname = hostname;
		this.port = port;
		this.atomicInteger = new AtomicInteger(0);
		this.queueService = queueService;
		this.readHandler = new CompletionHandler<Integer, ByteBuffer>() {
			@Override
			public void completed(Integer result, ByteBuffer buff) {
				try {
					buff.rewind();
					String handle = 
							ProtocolBufferUtils.readHandleFromBuffer(buff);
					MessageLiteSerializer<?> serializer = 
							NIOServer.this.queueService.getSerializer(handle);
					MessageLite msg = 
							ProtocolBufferUtils.readFromBuffer(buff,
										serializer.getBuilder(), 
										serializer.getRegistry());
					
					NIOServer.this.queueService.sendObjectToQueue(handle, msg);
				} catch (InvalidProtocolBufferException | 
						InterruptedException e) {
					failed(e, buff);
				}
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				LOG.error("Unable to read message!", exc);
			}
		};
		this.serverSocket = null;
	}
	
	/**
	 * Starts this service
	 * @return true is service was started, false otherwise.
	 * 
	 * @throws IOException 
	 */
	public void start() throws IOException {
		InetSocketAddress addr = 
				new InetSocketAddress(hostname, port);
		this.serverSocket = newServerSocket().bind(addr);
		LOG.info("Accepting connections at " + addr + " " + hostname); 
		acceptNextConnection();
	}

	
	private void acceptNextConnection() {
		this.serverSocket.accept(atomicInteger.incrementAndGet(), this);
	}
	
	/**
	 * Stops the service.
	 * @return true is service was stopped, false otherwise.
	 * @throws InterruptedException 
	 */
	public void shutdown() throws InterruptedException {
		this.channelGroup.shutdown();
		this.channelGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
	}
	
	@Override
	public void completed(AsynchronousSocketChannel result, Integer attachment){
		acceptNextConnection();
		LOG.info("Accepted connection #" + attachment); 
		ProtocolBufferUtils.readFromChannel(result, this.readHandler);
	}

	@Override
	public void failed(Throwable exc, Integer attachment) {
		LOG.error("Exception at connection #" + attachment, exc);
	}
	
	private AsynchronousServerSocketChannel newServerSocket() 
			throws IOException {
		return AsynchronousServerSocketChannel.open(this.channelGroup);
	}
}