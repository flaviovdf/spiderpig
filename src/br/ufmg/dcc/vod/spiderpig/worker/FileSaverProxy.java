package br.ufmg.dcc.vod.spiderpig.worker;

import java.io.IOException;

import br.ufmg.dcc.vod.spiderpig.common.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileWrapper;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.Payload;

public class FileSaverProxy implements FileSaver {

	private final RemoteMessageSender messageSender;
	private final ServiceID fileSaverID;

	public FileSaverProxy(ServiceID fileSaverID, RemoteMessageSender sender) {
		this.fileSaverID = fileSaverID;
		this.messageSender = sender;
	}

	@Override
	public void save(String fileID, byte[] payload) {
		Payload msg = FileWrapper.toProtocolBuffer(fileID, payload);
		this.messageSender.send(this.fileSaverID, msg);
	}

	@Override
	public int numSaved() {
		return 0;
	}

	@Override
	public boolean close() throws IOException {
		return true;
	}
}