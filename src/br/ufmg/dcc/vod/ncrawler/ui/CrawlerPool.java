package br.ufmg.dcc.vod.ncrawler.ui;

import java.util.HashMap;
import java.util.Map;

import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;

public class CrawlerPool {

	private static final Map<String, EvaluatorFactory<?,?,?>> crawlers = new HashMap<String, EvaluatorFactory<?,?,?>>();
	static {
		;
//		crawlers.put("YTAPI", new YTApiFactory());
//		crawlers.put("YTVIDAPI", new YTVideoApiFactory());
//		crawlers.put("LFM", new LFMApiFactory());
//		crawlers.put("LFM_MUSIC", new ArtistSongFactory());
//		crawlers.put("YOUTUBE_RESPONSE", new YoutubeResponseFactory());
//		crawlers.put("LFM_CATS", new LastFMCatsEvalFactory());
//		crawlers.put("YTHTMLSTATS", new YTHtmlStatsFactory());
//		crawlers.put("GPLUS", new PlusFactory());
//		crawlers.put("URL", new URLDownFactory());
	}
	
	public static EvaluatorFactory<?,?,?> get(String name) {
		return crawlers.get(name);
	}
}
