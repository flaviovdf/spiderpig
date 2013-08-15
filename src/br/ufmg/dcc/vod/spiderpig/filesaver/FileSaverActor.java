package br.ufmg.dcc.vod.spiderpig.filesaver;

import java.io.IOException;

import br.ufmg.dcc.vod.spiderpig.common.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.common.queue.serializer.MessageLiteSerializer;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Payload.UploadMessage;

public class FileSaverActor extends Actor<UploadMessage> 
		implements QueueProcessor<UploadMessage> {

	public static final String HANDLE = "FileSaverActor";
	private final FileSaver saver;

	public FileSaverActor(FileSaver saver) {
		super(HANDLE);
		this.saver = saver;
	}
	
	@Override
	public QueueProcessor<UploadMessage> getQueueProcessor() {
		return this;
	}

	@Override
	public MessageLiteSerializer<UploadMessage> newMsgSerializer() {
		return new MessageLiteSerializer<>(UploadMessage.newBuilder());
	}

	@Override
	public void process(UploadMessage t) {
		FileWrapper wrapper = FileWrapper.fromProtocolBuffer(t);
		saver.save(wrapper.getFileID(), wrapper.getFilePayload());
	}
	
	public boolean closeSaver() throws IOException {
		return this.saver.close();
	}
}