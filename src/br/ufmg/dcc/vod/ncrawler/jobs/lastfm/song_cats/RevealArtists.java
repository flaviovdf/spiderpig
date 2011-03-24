package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.song_cats;

import java.io.File;
import java.io.IOException;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.common.FileUtil;
import br.ufmg.dcc.vod.ncrawler.common.MyXStreamer;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api.AlbumDAO;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api.ArtistDAO;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api.GenericMusicDAO;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api.SongDAO;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api.GenericMusicDAO.Type;

public class RevealArtists {

	public static void main(String[] args) throws IOException {
		List<String> files;
		files = FileUtil.readFileToList(new File(args[0]));
		for (String s : files) {
			try {
				File f = new File("/data/users/flavio/lastfm/songs/songs/" + s);
				GenericMusicDAO gmd = (GenericMusicDAO) MyXStreamer.getInstance().fromXML(f);
				System.out.print(s + "  ");
				if (gmd.getT() == Type.ARTIST) {
					ArtistDAO ar = (ArtistDAO) gmd;
					System.out.println(ar.getName());
				} else if (gmd.getT() == Type.ALBUM) {
					AlbumDAO ab = (AlbumDAO) gmd;
					System.out.println(ab.getArtist());
				} else if (gmd.getT() == Type.SONG) {
					SongDAO sn = (SongDAO) gmd;
					System.out.println(sn.getArtist());
				}
			} catch (IOException e) {
			}
		}
	}
	
}
