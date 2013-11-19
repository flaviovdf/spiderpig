package br.ufmg.dcc.vod.spiderpig.filesaver;

import java.io.IOException;

import br.ufmg.dcc.vod.spiderpig.common.queue.Actor;
import br.ufmg.dcc.vod.spiderpig.common.queue.QueueProcessor;
import br.ufmg.dcc.vod.spiderpig.common.queue.serializer.MessageLiteSerializer;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.Payload;

public class FileSaverActor extends Actor<Payload> 
		implements QueueProcessor<Payload> {

	public static final String HANDLE = "FileSaverActor";
	private final FileSaver saver;

	public FileSaverActor(FileSaver saver) {
		super(HANDLE);
		this.saver = saver;
	}
	
	@Override
	public QueueProcessor<Payload> getQueueProcessor() {
		return this;
	}

	@Override
	public MessageLiteSerializer<Payload> newMsgSerializer() {
		return new MessageLiteSerializer<>(Payload.newBuilder());
	}

	@Override
	public void process(Payload t) {
		FileWrapper wrapper = FileWrapper.fromProtocolBuffer(t);
		saver.save(wrapper.getFileID(), wrapper.getFilePayload());
	}
	
	public boolean closeSaver() throws IOException {
		return this.saver.close();
	}
}