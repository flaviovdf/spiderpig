package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.user_apihtml;

import java.io.Serializable;
import java.util.Set;

public class LastFMTagDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String tag;
	
	private final Set<String> artists;
	private final Set<String> songs;
	private final Set<String> albums;

	public LastFMTagDAO(String tag, Set<String> artists, Set<String> albums, Set<String> songs) {
		this.tag = tag;
		this.artists = artists;
		this.albums = albums;
		this.songs = songs;
	}

	public String getTag() {
		return tag;
	}
	
	public Set<String> getArtists() {
		return artists;
	}
	
	public Set<String> getSongs() {
		return songs;
	}

	public Set<String> getAlbums() {
		return albums;
	}
	
	public int getUseCount() {
		return songs.size() + artists.size();
	}
}