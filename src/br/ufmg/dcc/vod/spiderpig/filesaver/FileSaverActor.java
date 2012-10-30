package br.ufmg.dcc.vod.spiderpig.filesaver;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Payload.UploadMessage;
import br.ufmg.dcc.vod.spiderpig.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.queue.serializer.MessageLiteSerializer;

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
}