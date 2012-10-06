package br.ufmg.dcc.vod.ncrawler.stats;

import java.util.HashMap;
import java.util.Map;

public class CompositeStatEvent implements StatEvent {

	private final Map<String, Integer> map;

	public CompositeStatEvent(String[] ids, int[] incs) {
		this(new HashMap<String, Integer>());
		
		for (int i = 0; i < ids.length; i++) {
			this.map.put(ids[i], incs[i]);
		}
	}
	
	public CompositeStatEvent(Map<String, Integer> map) {
		this.map = map;
	}
	
	@Override
	public Map<String, Integer> getIncrements() {
		return map;
	}

}
