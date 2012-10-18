package br.ufmg.dcc.vod.ncrawler.distributed.filesaver;

import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.MessageListener;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileWrapper;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Payload.UploadMessage;

import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.MessageLite.Builder;

/**
 * Receives upload messages and proxies them to a {@link FileSaver}
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class UploadListener implements MessageListener<UploadMessage> {

	private final FileSaver saver;
	
	/**
	 * Creates a new listener which will use the given {@link FileSaver}
	 * 
	 * @param saver File saver to use
	 */
	public UploadListener(FileSaver saver){
		this.saver = saver;
	}

	@Override
	public void receiveMessage(UploadMessage msg) {
		FileWrapper fileWrapper = FileWrapper.fromProtocolBuffer(msg);
		String fname = fileWrapper.getFileID();
		saver.save(fname, fileWrapper.getFilePayload());
	}

	@Override
	public ExtensionRegistryLite getRegistry() {
		return null;
	}

	@Override
	public Builder getNewBuilder() {
		return UploadMessage.newBuilder();
	}
}