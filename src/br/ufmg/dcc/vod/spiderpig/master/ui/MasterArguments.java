package br.ufmg.dcc.vod.spiderpig.master.ui;

import java.util.List;

import br.ufmg.dcc.vod.spiderpig.Crawler;
import br.ufmg.dcc.vod.spiderpig.common.config.Arguments;

public class MasterArguments implements Arguments {

	private final Crawler crawler;
	private final List<String> seed;

	public MasterArguments(Crawler crawler, List<String> seed) {
		this.crawler = crawler;
		this.seed = seed;
	}

	public Crawler getCrawler() {
		return crawler;
	}
	
	public List<String> getSeed() {
		return seed;
	}
	
}
