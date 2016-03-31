package br.ufmg.dcc.vod.spiderpig.filesaver;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.Payload;

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
    
    public Payload toProtocolBuffer() {
        return Payload.newBuilder()
                .setPayloadFileName(this.fileID)
                .setPayloadFile(ByteString.copyFrom(this.filePayload))
                .build();
    }
    
    public static FileWrapper fromByteArray(byte[] data) 
            throws InvalidProtocolBufferException {
        return fromProtocolBuffer(Payload.parseFrom(data));
    }

    public static FileWrapper fromProtocolBuffer(Payload uploadMessage) {
        String fileID = uploadMessage.getPayloadFileName();
        byte[] filePayload = uploadMessage.getPayloadFile().toByteArray();
        return new FileWrapper(fileID, filePayload);
    }
    
    public static Payload toProtocolBuffer(String fileID,
            byte[] filePayload) {
        return Payload.newBuilder()
                .setPayloadFileName(fileID)
                .setPayloadFile(ByteString.copyFrom(filePayload))
                .build();
    }
}
