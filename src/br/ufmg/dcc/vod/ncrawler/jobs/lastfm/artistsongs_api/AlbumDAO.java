package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api;

import java.util.Collection;
import java.util.Date;

public class AlbumDAO  extends GenericMusicDAO {

	private static final long serialVersionUID = 1L;
	private final String artist;
	private final Date releaseDate;
	
	public AlbumDAO(Type t, String name, Collection<String> tags,
			String wikiSummary, String wikiText,
			Date wikiLastChanged, int listeners, int playcount, String mbid,
			boolean streamable, String url, String artist, Date releaseDate) {
		super(t, name, tags, wikiSummary, wikiText, wikiLastChanged,
				listeners, playcount, mbid, streamable, url);
		this.artist = artist;
		this.releaseDate = releaseDate;
	}
	
	public String getArtist() {
		return artist;
	}
	
	public Date getReleaseDate() {
		return releaseDate;
	}
}
