package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.user_apihtml;

import java.io.Serializable;

public class LastFMArtistDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String url;
	private final int playcount;

	public LastFMArtistDAO(String url, int playcount) {
		this.url = url;
		this.playcount = playcount;
	}
	
	public String getUrl() {
		return url;
	}
	
	public int getPlaycount() {
		return playcount;
	}
	
	@Override
	public String toString() {
		return url + ":" + playcount;
	}
}
