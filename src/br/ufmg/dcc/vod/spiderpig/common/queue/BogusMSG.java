package br.ufmg.dcc.vod.spiderpig.common.queue;

import java.io.IOException;
import java.io.OutputStream;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

class BogusMSG implements MessageLite {
    
    @Override 
    public Parser<? extends MessageLite> getParserForType() {
        return null;
    }

	@Override
	public MessageLite getDefaultInstanceForType() {
		return null;
	}

	@Override
	public boolean isInitialized() {
		return false;
	}

	@Override
	public void writeTo(CodedOutputStream output) throws IOException {
	}

	@Override
	public int getSerializedSize() {
		return 0;
	}

	@Override
	public ByteString toByteString() {
		return null;
	}

	@Override
	public byte[] toByteArray() {
		return null;
	}

	@Override
	public void writeTo(OutputStream output) throws IOException {
	}

	@Override
	public void writeDelimitedTo(OutputStream output) throws IOException {
	}

	@Override
	public Builder newBuilderForType() {
		return null;
	}

	@Override
	public Builder toBuilder() {
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof BogusMSG;
	}
	
	@Override
	public int hashCode() {
		return 1;
	}
}
