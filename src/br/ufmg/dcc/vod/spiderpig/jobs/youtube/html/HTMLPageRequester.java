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
	public byte[] performRequest(String crawlID) throws Exception {
		URL videoUrl = new URL("http://www.youtube.com/watch?v=" + crawlID +
				"&gl=US&hl=en");
		String header = "<crawledvideoid = " + crawlID + ">";
		String footer = "</crawledvideoid>";
		return this.getter.getHtml(videoUrl, header, footer);
	}
}