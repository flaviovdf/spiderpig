package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import net.roarsoftware.lastfm.Album;
import net.roarsoftware.lastfm.Artist;
import net.roarsoftware.lastfm.Caller;
import net.roarsoftware.lastfm.Track;
import net.roarsoftware.lastfm.cache.FileSystemCache;
import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api.GenericMusicDAO.Type;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.user_apihtml.LastFMApiCrawlJob;

public class ArtistSongCrawlJob implements CrawlJob {

	private Evaluator e;
	
	private final String toCollect;

	public ArtistSongCrawlJob(String toCollect) {
		this.toCollect = toCollect;
	}
	
	@Override
	public void collect() {
		File cacheDir = new File("/tmp/lastfm-cache/");
		Caller.getInstance().setCache(new FileSystemCache(cacheDir));
		
		try {
			Pair<Type, Pair<String, String>> parse = 
				ArtistSongUtils.parseCollectString(toCollect);
	
			Type type = parse.first;
			Pair<String, String> data = parse.second;
			String artistDecode = ArtistSongUtils.double_decode(data.first);
			String trackOrAlbumDecode = null;
			if (type != Type.ARTIST) {
				trackOrAlbumDecode = ArtistSongUtils.double_decode(data.second);
			}
			
			if (type == Type.ARTIST) {
				Artist info = Artist.getInfo(artistDecode, LastFMApiCrawlJob.API_KEY);
				
				if (Caller.getInstance().getLastResult().isSuccessful()) {
					String name = info.getName();
					Collection<String> tags = info.getTags();
					String wikiSummary = info.getWikiSummary();
					String wikiText = info.getWikiText();
					Date wikiLastChanged = info.getWikiLastChanged();
					int listeners = info.getListeners();
					int playcount = info.getPlaycount();
					String mbid = info.getMbid();
					boolean streamable = info.isStreamable();
					String url = info.getUrl();
			
					ArtistDAO dao = new ArtistDAO(type, name, tags, wikiSummary, wikiText, wikiLastChanged, listeners, playcount, mbid, streamable, toCollect);
					e.evaluteAndSave(toCollect, dao);
				} else {
					e.error(toCollect, new UnableToCollectException(Caller.getInstance().getLastResult().getErrorMessage()));
				}
				
			} else if (type == Type.ALBUM) {
				Album info = Album.getInfo(artistDecode, trackOrAlbumDecode, LastFMApiCrawlJob.API_KEY);
				
				if (Caller.getInstance().getLastResult().isSuccessful()) {
					String name = info.getName();
					Collection<String> tags = info.getTags();
					String wikiSummary = info.getWikiSummary();
					String wikiText = info.getWikiText();
					Date wikiLastChanged = info.getWikiLastChanged();
					int listeners = info.getListeners();
					int playcount = info.getPlaycount();
					String mbid = info.getMbid();
					boolean streamable = info.isStreamable();
					String url = info.getUrl();
					
					String artist = info.getArtist();
					Date releaseDate = info.getReleaseDate();
					
					AlbumDAO dao = new AlbumDAO(type, name, tags, wikiSummary, wikiText, wikiLastChanged, listeners, playcount, mbid, streamable, toCollect, artist, releaseDate);
					e.evaluteAndSave(toCollect, dao);
				} else {
					e.error(toCollect, new UnableToCollectException(Caller.getInstance().getLastResult().getErrorMessage()));
				}
			} else if (type == Type.SONG) {
				Track info = Track.getInfo(artistDecode, trackOrAlbumDecode, LastFMApiCrawlJob.API_KEY);
				
				if (Caller.getInstance().getLastResult().isSuccessful()) {
					String name = info.getName();
					Collection<String> tags = info.getTags();
					String wikiSummary = info.getWikiSummary();
					String wikiText = info.getWikiText();
					Date wikiLastChanged = info.getWikiLastChanged();
					int listeners = info.getListeners();
					int playcount = info.getPlaycount();
					String mbid = info.getMbid();
					boolean streamable = info.isStreamable();
					String url = info.getUrl();
					
					String artist = info.getArtist();
					String album = info.getAlbum();
					int duration = info.getDuration();
					int position = info.getPosition();
					Date playedWhen = info.getPlayedWhen();
					
					SongDAO dao = new SongDAO(type, name, tags, wikiSummary, wikiText, wikiLastChanged, listeners, playcount, mbid, streamable, toCollect, artist, album, duration, position, playedWhen);
					e.evaluteAndSave(toCollect, dao);
				} else {
					e.error(toCollect, new UnableToCollectException(Caller.getInstance().getLastResult().getErrorMessage()));
				}
			}
		} catch (IOException ioe) {
			e.error(toCollect, new UnableToCollectException(ioe.getMessage()));
		}
	}

	@Override
	public void setEvaluator(Evaluator e) {
		this.e = e;
	}

	@Override
	public String getID() {
		return this.toCollect;
	}
}
