package br.ufmg.dcc.vod.spiderpig.distributed.nio.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.queue.common.ProtocolBufferUtils;

import com.google.protobuf.MessageLite;

public class RemoteMessageSender {
	
	private static final Logger LOG = Logger.getLogger(RemoteMessageSender.class);
	
	public void send(ServiceID serviceID, MessageLite msg) {
		String receiverHost = serviceID.getHostname();
		int receiverPort = serviceID.getPort();
		String handle = serviceID.getHandle();
		
		InetSocketAddress remoteAddr = 
				new InetSocketAddress(receiverHost, receiverPort);
		Socket socket = null;
		try {
			socket = new Socket();
			socket.connect(remoteAddr);
			
			ProtocolBufferUtils.msgToStream(handle, msg, 
					socket.getOutputStream());
			LOG.debug("Message Sent " + msg + " to " + serviceID);
		} catch (IOException e) {
			LOG.error("Failure at Message " + msg + " to " + serviceID, e);
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) { 
					LOG.error("Unable to close socket", e);
				}
			}
		}
	}
}