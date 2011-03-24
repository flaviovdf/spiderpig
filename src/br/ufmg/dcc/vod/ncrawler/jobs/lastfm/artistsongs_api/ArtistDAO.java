package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api;

import java.util.Collection;
import java.util.Date;

public class ArtistDAO extends GenericMusicDAO {
	
	private static final long serialVersionUID = 1L;
	
	public ArtistDAO(Type t, String name, Collection<String> tags,
			String wikiSummary, String wikiText,
			Date wikiLastChanged, int listeners, int playcount, String mbid,
			boolean streamable, String url) {
		super(t, name, tags, wikiSummary, wikiText, wikiLastChanged,
				listeners, playcount, mbid, streamable, url);
	}

}
