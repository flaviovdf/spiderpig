package br.ufmg.dcc.vod.spiderpig.distributed;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.queue.common.ProtocolBufferUtils;

import com.google.protobuf.MessageLite;

public class RemoteMessageSender {
	
	private static final Logger LOG = Logger.getLogger(RemoteMessageSender.class);
	
	public void send(ServiceID serviceID, MessageLite msg) {
		if (LOG.isDebugEnabled())
			LOG.debug("Sending " + msg + " to " + serviceID);
		String receiverHost = serviceID.getHostname();
		int receiverPort = serviceID.getPort();
		String handle = serviceID.getHandle();
		
		InetSocketAddress remoteAddr = 
				new InetSocketAddress(receiverHost, receiverPort);
		OutputStream outputStream = null;
		Socket socket = null;
		try {
			socket = new Socket();
			socket.connect(remoteAddr);
			
			outputStream = socket.getOutputStream();
			ProtocolBufferUtils.msgToStream(handle, msg, 
					outputStream);
			if (LOG.isDebugEnabled())
				LOG.debug("Message Sent " + msg + " to " + serviceID);
		} catch (IOException e) {
			if (LOG.isDebugEnabled())
				LOG.debug("Failure at Message " + msg + " to " + serviceID, e);
		} finally {
			try {
				if (outputStream != null)
					outputStream.close();
				if (socket != null)
					socket.close();
			} catch (IOException e) { 
				if (LOG.isDebugEnabled())
					LOG.debug("Unable to close socket", e);
			}
		}
	}
}