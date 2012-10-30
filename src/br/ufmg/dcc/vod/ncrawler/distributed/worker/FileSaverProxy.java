package br.ufmg.dcc.vod.ncrawler.distributed.worker;

import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.RemoteMessageSender;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileWrapper;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Ids.ServiceID;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Payload.UploadMessage;

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