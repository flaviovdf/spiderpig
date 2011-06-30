package br.ufmg.dcc.vod.ncrawler.jobs.plus;

import java.io.Serializable;
import java.util.Set;

public class PlusDao implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final byte[] content;
	private final Set<String> discoveredIds;
	
	public PlusDao(byte[] content, Set<String> discoveredIds) {
		this.content = content;
		this.discoveredIds = discoveredIds;
	}
	
	public byte[] getContent() {
		return content;
	}
	
	public Set<String> getDiscoveredIds() {
		return discoveredIds;
	}
}