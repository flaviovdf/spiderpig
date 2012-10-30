package br.ufmg.dcc.vod.spiderpig.distributed.nio.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.queue.common.ProtocolBufferUtils;

import com.google.protobuf.MessageLite;

public class RemoteMessageSender {
	
	private static final Logger LOG = Logger.getLogger(RemoteMessageSender.class);
	private final AtomicInteger atomicInteger = new AtomicInteger(0);
	
	public void send(ServiceID serviceID, MessageLite msg) {
		int attachment = atomicInteger.incrementAndGet();
		
		String receiverHost = serviceID.getHostname();
		int receiverPort = serviceID.getPort();
		String handle = serviceID.getHandle();
		
		InetSocketAddress remoteAddr = 
				new InetSocketAddress(receiverHost, receiverPort);
		AsynchronousSocketChannel socket = null;
		try {
			socket = AsynchronousSocketChannel.open();
			socket.connect(remoteAddr).get();
			
			ByteBuffer buffer = 
					ProtocolBufferUtils.msgToSizedByteBuffer(handle, msg);
			Future<Integer> write = socket.write(buffer);
			write.get();
			LOG.debug("Message Sent #" + attachment + " to " + serviceID);
		} catch (IOException | InterruptedException | ExecutionException e) {
			LOG.error("Failure at Message #" + attachment + " to " + serviceID
					, e);
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) { }
			}
		}
	}
}