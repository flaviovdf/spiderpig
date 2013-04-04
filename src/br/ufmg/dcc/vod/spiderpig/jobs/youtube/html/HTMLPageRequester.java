package br.ufmg.dcc.vod.spiderpig.jobs.youtube.html;

import java.net.URL;

import br.ufmg.dcc.vod.spiderpig.common.URLGetter;
import br.ufmg.dcc.vod.spiderpig.jobs.Requester;

public class HTMLPageRequester implements Requester<byte[]> {

	private URLGetter getter;

	public HTMLPageRequester(String devKey) {
		this.getter = new URLGetter();
		this.getter.setProperty("User-Agent", 
				"Research-Crawler-APIDEVKEY-" + devKey);
	}
	
	@Override
	public byte[] performRequest(String url) throws Exception {
		URL videoUrl = new URL(url);
		String header = "<crawledvideoid = " + url + ">";
		String footer = "</crawledvideoid>";
		return this.getter.getHtml(videoUrl, header, footer);
	}
}
