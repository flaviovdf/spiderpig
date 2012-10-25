package br.ufmg.dcc.vod.ncrawler;

import java.util.List;

public interface Crawler {

	public void dispatch(List<String> seed);

	public void dispatch(String... seed);
	
	public void crawl();
	
}
