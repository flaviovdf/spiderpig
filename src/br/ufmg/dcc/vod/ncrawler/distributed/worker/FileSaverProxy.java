package br.ufmg.dcc.vod.ncrawler.distributed.worker;

import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.NIOMessageSender;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileWrapper;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Payload.UploadMessage;

public class FileSaverProxy implements FileSaver {

	private final String fileSaverHost;
	private final int fileSaverPort;
	private final NIOMessageSender messageSender;

	public FileSaverProxy(String fileSaverHost, int fileSaverPort) {
		this.fileSaverHost = fileSaverHost;
		this.fileSaverPort = fileSaverPort;
		this.messageSender = new NIOMessageSender();
	}

	@Override
	public void save(String fileID, byte[] payload) {
		UploadMessage msg = FileWrapper.toProtocolBuffer(fileID, payload);
		this.messageSender.send(fileSaverHost, fileSaverPort, msg);
	}

}
