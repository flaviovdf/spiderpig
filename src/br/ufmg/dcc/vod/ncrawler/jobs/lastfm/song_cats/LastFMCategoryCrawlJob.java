package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.song_cats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api.ArtistSongUtils;

public class LastFMCategoryCrawlJob implements CrawlJob {
	
	private Evaluator eval;
	private final String id;
	
	public LastFMCategoryCrawlJob(String id) {
		this.id = id;
	}
	
	@Override
	public void collect() {
		BufferedReader bis = null;
		try {
			URL u = new URL(getURL());
			URLConnection c = u.openConnection();
            c.setRequestProperty("User-Agent", "ResearchCrawler-contact-flaviov_at_dcc.ufmg.br");
			c.connect();
			InputStream content = c.getInputStream();
			
			bis = new BufferedReader(new InputStreamReader(content));
			String line = null;
			
			List<Pair<String, String>> rv = new ArrayList<Pair<String,String>>();
			while ((line = bis.readLine()) != null) {
				if (line.startsWith("<div id=\"artistsearchresults\">")) {
					String[] split = line.split("<TD class=\"cell\"");
					for (int i = 1; i < split.length; i++) {
						String s = split[i];
						if (s.contains("sql") && !s.contains("speaker.gif")) {
							String[] aNameSplit = s.split("\">");
							String artistName = aNameSplit[aNameSplit.length - 1].split("</a>")[0];
							String artistGenre = split[i+1].split(">")[1].split("<")[0];
							
							rv.add(new Pair<String, String>(artistName, artistGenre));
						}
						
					}
					
					break;
				}
			}
//            System.out.println(u);
//           System.out.println(rv);
			eval.evaluteAndSave(id, rv);
		} catch (IOException e) {
			eval.error(id, new UnableToCollectException(e.getLocalizedMessage()));
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
				}
		}
		
	}

	private String getURL() throws UnsupportedEncodingException {
		return "http://www.allmusic.com/cg/amg.dll?p=amg&sql=1:" + ArtistSongUtils.encode(id) + "~C";
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public void setEvaluator(Evaluator e) {
		this.eval = e;
	}

    public static void main(String[] args) {
        new LastFMCategoryCrawlJob("Radiohead").collect();    
    }

//	private static void extractAlbum(String albumUrl) throws IOException {
//		Pattern p = Pattern.compile("\\d\\d\\d\\d");
//		
//		final String ARTIST_ID = "206px";
//		final String ALBUM_ID = "230px";
//		final String LABEL_ID = "89px";
//		final String GENRE_ID = "65px";
//		
//		BufferedReader bis = null;
//		try {
//			URL u = new URL(albumUrl);
//			URLConnection c = u.openConnection();
//			c.connect();
//			InputStream content = c.getInputStream();
//			
//			bis = new BufferedReader(new InputStreamReader(content));
//			String line = null;
//			while ((line = bis.readLine()) != null) {
//				if (line.startsWith("<div id=\"albumsearchresults\">")) {
//					String[] split = line.split("<td class=\"cell\">|</TD>|</a>");
//					
//					int year = -1;
//					String artistName = null;
//					String albumName = null;
//					String labelName = null;
//					String genreName = null;
//					
//					for (int i = 0; i < split.length; i++) {
//						String s = split[i];
//						Matcher matcher = p.matcher(s);
//
//						if (matcher.matches()) {
//							year = Integer.parseInt(s);
//						}
//						
//						String[] anotherSplit = s.split("\">");
//						if (year != -1 && s.contains(ARTIST_ID)) {
//							artistName = anotherSplit[anotherSplit.length - 1];
//						} else if (year != -1 && s.contains(ALBUM_ID)) {
//							albumName = anotherSplit[anotherSplit.length - 1];
//						} else if (year != -1 && s.contains(LABEL_ID)) {
//							labelName = anotherSplit[anotherSplit.length - 1];
//						} else if (year != -1 && s.contains(GENRE_ID)) {
//							genreName = anotherSplit[anotherSplit.length - 1];
//							
//							System.out.println(year);
//							System.out.println(artistName);
//							System.out.println(albumName);
//							System.out.println(labelName);
//							System.out.println(genreName);
//							System.out.println();
//							
//							year = -1;
//						}
//					}
//					
//					break;
//				}
//			}
//		} finally {
//			if (bis != null) bis.close();
//		}
//	}	
}
