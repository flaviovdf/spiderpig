package br.ufmg.dcc.vod.ncrawler.distributed.nio.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.distributed.nio.common.ProtocolBufferUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

public class NIOServer<T extends MessageLite> extends AbstractNIOService
		implements CompletionHandler<AsynchronousSocketChannel, Integer> {

	private static final Logger LOG = Logger.getLogger(NIOServer.class);
	
	private final AtomicInteger atomicInteger;
	private final String hostname;
	private final int port;
	private final CompletionHandler<Integer, ByteBuffer> readHandler;
	
	protected AsynchronousServerSocketChannel serverSocket;
	private final MessageListener<T> listener;

	public NIOServer(int nThreads, String hostname, final int port, 
			MessageListener<T> listener) {
		super(nThreads);
		this.hostname = hostname;
		this.port = port;
		this.listener = listener;
		this.atomicInteger = new AtomicInteger(0);
		this.readHandler = new CompletionHandler<Integer, ByteBuffer>() {
			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				MessageListener<T> listener = NIOServer.this.listener;
				try {
					MessageLite msg = 
							ProtocolBufferUtils.readFromBuffer(attachment,
					listener.getNewBuilder(), listener.getRegistry());
					listener.receiveMessage(((T) msg));
				} catch (InvalidProtocolBufferException e) {
					failed(e, attachment);
				}
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				LOG.error("Unable to read message!", exc);
			}
		};
		this.serverSocket = null;
	}
	
	@Override
	public void startHooks() throws IOException {
		InetSocketAddress addr = new InetSocketAddress(hostname, port);
		this.serverSocket = newServerSocket().bind(addr);
		acceptNextConnection();
	}

	private void acceptNextConnection() {
		this.serverSocket.accept(atomicInteger.incrementAndGet(), this);
	}
	
	@Override
	public void shutdownHooks() throws IOException {
		this.serverSocket.close();
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
}