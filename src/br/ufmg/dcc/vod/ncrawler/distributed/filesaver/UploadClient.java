package br.ufmg.dcc.vod.ncrawler.distributed.filesaver;

import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.NIOMessageSender;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileWrapper;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Payload.UploadMessage;

/**
 * Uploads files to a remote server
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class UploadClient {
	
	private final NIOMessageSender sender;
	
	/**
	 * Creates a new client which will send messages with the given sender
	 * 
	 * @param sender Sender to use for communication
	 */
	public UploadClient(NIOMessageSender sender) {
		this.sender = sender;
	}

	/**
	 * Sends the file within the given wrapper to the server at the given
	 * host and port.
	 * 
	 * @param wrapper Wrapper containing the file
	 * 
	 * @param remoteHost Host to send to
	 * @param remotePort Port to send to
	 */
	public void send(FileWrapper wrapper, String remoteHost, int remotePort) {
		UploadMessage msg = wrapper.toProtocolBuffer();
		this.sender.send(remoteHost, remotePort, msg);
	}
}