package br.ufmg.dcc.vod.spiderpig.filesaver;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Payload.UploadMessage;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class FileWrapper {

	private final String fileID;
	private final byte[] filePayload;

	public FileWrapper(String fileID, byte[] filePayload) {
		this.fileID = fileID;
		this.filePayload = filePayload;
	}

	public String getFileID() {
		return fileID;
	}
	
	public byte[] getFilePayload() {
		return filePayload;
	}
	
	public UploadMessage toProtocolBuffer() {
		return UploadMessage.newBuilder()
				.setFileName(this.fileID)
				.setPayload(ByteString.copyFrom(this.filePayload))
				.build();
	}
	
	public static FileWrapper fromByteArray(byte[] data) 
			throws InvalidProtocolBufferException {
		return fromProtocolBuffer(UploadMessage.parseFrom(data));
	}

	public static FileWrapper fromProtocolBuffer(UploadMessage uploadMessage) {
		String fileID = uploadMessage.getFileName();
		byte[] filePayload = uploadMessage.getPayload().toByteArray();
		return new FileWrapper(fileID, filePayload);
	}
	
	public static UploadMessage toProtocolBuffer(String fileID,
			byte[] filePayload) {
		return UploadMessage.newBuilder()
				.setFileName(fileID)
				.setPayload(ByteString.copyFrom(filePayload))
				.build();
	}
}
