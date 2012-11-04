package br.ufmg.dcc.vod.spiderpig.worker;

import br.ufmg.dcc.vod.spiderpig.distributed.RemoteMessageSender;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileWrapper;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Payload.UploadMessage;

public class FileSaverProxy implements FileSaver {

	private final RemoteMessageSender messageSender;
	private final ServiceID fileSaverID;

	public FileSaverProxy(ServiceID fileSaverID, RemoteMessageSender sender) {
		this.fileSaverID = fileSaverID;
		this.messageSender = sender;
	}

	@Override
	public void save(String fileID, byte[] payload) {
		UploadMessage msg = FileWrapper.toProtocolBuffer(fileID, payload);
		this.messageSender.send(this.fileSaverID, msg);
	}

	@Override
	public int numSaved() {
		return 0;
	}
}