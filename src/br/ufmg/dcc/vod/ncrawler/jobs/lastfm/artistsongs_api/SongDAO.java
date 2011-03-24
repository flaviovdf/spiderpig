package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api;

import java.util.Collection;
import java.util.Date;

public class SongDAO extends GenericMusicDAO {

	private static final long serialVersionUID = 1L;
	private final String artist;
	private final String album;
	private final int duration;
	private final int position;
	private final Date playedWhen;
	
	public SongDAO(Type t, String name, Collection<String> tags,
			String wikiSummary, String wikiText,
			Date wikiLastChanged, int listeners, int playcount, String mbid,
			boolean streamable, String url, 
			String artist, String album, int duration, int position, Date playedWhen) {
		super(t, name, tags, wikiSummary, wikiText, wikiLastChanged,
				listeners, playcount, mbid, streamable, url);
		this.artist = artist;
		this.album = album;
		this.duration = duration;
		this.position = position;
		this.playedWhen = playedWhen;
	}


	public String getArtist() {
		return artist;
	}
	
	public String getAlbum() {
		return album;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public int getPosition() {
		return position;
	}
	
	public Date getPlayedWhen() {
		return playedWhen;
	}
	
}
