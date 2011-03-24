package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api.GenericMusicDAO.Type;

public class ArtistSongUtils {

	public static Pair<Type, Pair<String, String>> parseCollectString(String s) {
		String replaceAll = s.replaceAll("\\+noredirect/", "");
		String[] split = replaceAll.split(":");
		String type = split[0];
		String collect = split[1].replaceAll("/music/", "").trim();
		
		Type t = null;
		String first = null;
		String second = null;
		
		if (type.equals("Ar")) {
			String artistName = collect;
			
			first = artistName;
			t = Type.ARTIST;
		} else if (type.equals("Ab")) {
			String[] abSplit = collect.split("/");
			String artistName = abSplit[0];
			String albumName = abSplit[1];
			
			first = artistName;
			second = albumName;
			t = Type.ALBUM;
		} else if (type.equals("Sn")) {
			String[] songSplit = collect.split("/");
			String artistName = songSplit[0];
			String songName = songSplit[2];
			
			first = artistName;
			second = songName;
			t = Type.SONG;
		}

		Pair<Type, Pair<String, String>> rv = new Pair<Type, Pair<String, String>>(t, new Pair<String, String>(first, second));
		return rv;
	}

	public static String double_decode(String s) throws UnsupportedEncodingException {
		String dec = single_decode(s);
		
		if (dec.contains("%")) {
			dec = single_decode(dec);
		}
		
		return dec;
	}

	public static String single_decode(String s) throws UnsupportedEncodingException {
		String dec = URLDecoder.decode(s, "UTF-8");
		return dec;
	}
	
	public static void main(String[] args) throws IOException {
		BufferedReader br = null;
		String line;
		
		try	{
			br = new BufferedReader(new FileReader(new File("/tmp/arab")));
			while((line = br.readLine()) != null) {
				System.out.println(line);
				
				Pair<Type, Pair<String, String>> parse = 
					ArtistSongUtils.parseCollectString(line);
		
				Type type = parse.first;
				Pair<String, String> data = parse.second;
				String artistDecode = ArtistSongUtils.double_decode(data.first);
				String trackOrAlbumDecode = null;
				if (type != Type.ARTIST)
					trackOrAlbumDecode = ArtistSongUtils.double_decode(data.second);
				
				System.out.println(type);
				System.out.println(artistDecode);
				System.out.println(trackOrAlbumDecode);
				System.out.println();
			}
		} finally {
			if(br != null) {
				br.close();
			}
		}
	}

	public static String encode(String s) throws UnsupportedEncodingException {
		return URLEncoder.encode(s, "UTF-8");
	}
}
