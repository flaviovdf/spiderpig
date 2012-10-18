package br.ufmg.dcc.vod.ncrawler.distributed.nio.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.distributed.nio.common.ProtocolBufferUtils;

import com.google.protobuf.MessageLite;

public class NIOMessageSender extends AbstractNIOService
		implements CompletionHandler<Integer, Integer>{
	
	private static final Logger LOG = Logger.getLogger(NIOMessageSender.class);
	
	private final AtomicInteger atomicInteger = new AtomicInteger(0);
	
	public void send(String receiverHost, int receiverPort, MessageLite msg) {
		
		int attachment = atomicInteger.incrementAndGet();
		
		InetSocketAddress remoteAddr = 
				new InetSocketAddress(receiverHost, receiverPort);
		AsynchronousSocketChannel socket = null;
		try {
			socket = newSocket();
			socket.connect(remoteAddr).get();
			
			ByteBuffer buffer = 
					ProtocolBufferUtils.msgToSizedByteBuffer(msg);
			socket.write(buffer, attachment, this);
		} catch (IOException | InterruptedException | ExecutionException e) {
			this.failed(e, attachment);
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) { }
			}
		}
	}
	
	@Override
	public void completed(Integer result, Integer attachment) {
		LOG.info("Message Sent #" + attachment.toString());
	}

	@Override
	public void failed(Throwable exc, Integer attachment) {
		LOG.error("Failure at Message #" + attachment.toString(), exc);
	}
	
	@Override
	public void startHooks() throws IOException {
	}
	
	@Override
	public void shutdownHooks() throws IOException {
	}
}