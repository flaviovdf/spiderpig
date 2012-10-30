package br.ufmg.dcc.vod.spiderpig.queue.serializer;

import br.ufmg.dcc.vod.spiderpig.queue.QueueServiceException;

import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLite.Builder;

public class MessageLiteSerializer<T extends MessageLite> 
		implements Serializer<T> {

	private final Builder builder;
	private final ExtensionRegistryLite reg;

	public MessageLiteSerializer(Builder builder, ExtensionRegistryLite reg) {
		this.builder = builder;
		this.reg = reg;
	}

	public MessageLiteSerializer(Builder builder) {
		this(builder, null);
	}
	
	@Override
	public byte[] toByteArray(T t) {
		return t.toByteArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T interpret(byte[] take) {
		Builder merged = builder.clear();
		try {
			merged = merged.mergeFrom(take, reg);
			MessageLite build = merged.build();
			return (T) build;
		} catch (InvalidProtocolBufferException e) {
			throw new QueueServiceException(e);
		}
	}
	
	public Builder getBuilder() {
		return builder;
	}
	
	public ExtensionRegistryLite getRegistry() {
		return reg;
	}	
}