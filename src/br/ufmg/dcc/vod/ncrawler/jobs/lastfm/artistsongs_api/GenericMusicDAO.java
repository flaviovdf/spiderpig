package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

public abstract class GenericMusicDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Type t;
	private final String name;
	private final Collection<String> tags;
	private final String wikiSummary;
	private final String wikiText;
	private final Date wikiLastChanged;
	private final int listeners;
	private final int playcount;
	private final String mbid;
	private final boolean streamable;
	private final String url;


	public enum Type {ARTIST, SONG, ALBUM}
	
	public GenericMusicDAO(Type t, String name, Collection<String> tags,
			String wikiSummary, String wikiText, Date wikiLastChanged, 
			int listeners, int playcount, String mbid, boolean streamable, String url) {
				this.t = t;
				this.name = name;
				this.tags = tags;
				this.wikiSummary = wikiSummary;
				this.wikiText = wikiText;
				this.wikiLastChanged = wikiLastChanged;
				this.listeners = listeners;
				this.playcount = playcount;
				this.mbid = mbid;
				this.streamable = streamable;
				this.url = url;
	}

	public Type getT() {
		return t;
	}

	public String getName() {
		return name;
	}

	public Collection<String> getTags() {
		return tags;
	}

	public String getWikiSummary() {
		return wikiSummary;
	}

	public String getWikiText() {
		return wikiText;
	}

	public Date getWikiLastChanged() {
		return wikiLastChanged;
	}

	public int getListeners() {
		return listeners;
	}

	public int getPlaycount() {
		return playcount;
	}

	public String getMbid() {
		return mbid;
	}

	public boolean isStreamable() {
		return streamable;
	}

	public String getUrl() {
		return url;
	}
}
