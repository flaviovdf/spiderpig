package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api;

import java.io.File;
import java.util.Collection;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.MyXStreamer;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractEvaluator;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api.GenericMusicDAO.Type;

public class ArtistSongEvaluator extends AbstractEvaluator<GenericMusicDAO> {

	private Collection<String> toCollect;
	private File savePath;

	public ArtistSongEvaluator(Collection<String> toCollect, File savePath) {
		this.toCollect = toCollect;
		this.savePath = savePath;
	}
	
	@Override
	public CrawlJob createJob(String next) {
		return new ArtistSongCrawlJob(next);
	}

	@Override
	public Collection<String> getSeeds() {
		return toCollect;
	}

	@Override
	public Collection<String> realEvaluateAndSave(String collectID,	GenericMusicDAO collectContent) throws Exception {
		File out = null;
		Pair<Type, Pair<String, String>> parseCollect = ArtistSongUtils.parseCollectString(collectID);
		Type type = parseCollect.first;
		Pair<String, String> data = parseCollect.second;
		String artist = data.first;
		String trackOrAlbum = data.second;
		
		if (type == Type.ARTIST) {
			File dir = new File(savePath + File.separator + "ar");
			dir.mkdirs();
			out = new File(dir + File.separator + artist);
		} else if (type == Type.ALBUM) {
			File dir = new File(savePath + File.separator + "ab" + File.separator + artist);
			dir.mkdirs();
			out = new File(dir + File.separator + trackOrAlbum);		
		} else {
			File dir = new File(savePath + File.separator + "sn" + File.separator + artist);
			dir.mkdirs();
			out = new File(dir + File.separator + trackOrAlbum);
		}
		
		
		MyXStreamer.getInstance().toXML(collectContent, out);
		return null;
	}
}
