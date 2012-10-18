package br.ufmg.dcc.vod.ncrawler.distributed.nio.service;

import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLite.Builder;

public interface MessageListener<T extends MessageLite> {

	public abstract void receiveMessage(T msg);
	
	public abstract ExtensionRegistryLite getRegistry();
	
	public abstract Builder getNewBuilder();
	
}