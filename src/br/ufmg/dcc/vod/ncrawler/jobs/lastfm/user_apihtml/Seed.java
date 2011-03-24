package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.user_apihtml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.roarsoftware.lastfm.Artist;
import net.roarsoftware.lastfm.Tag;
import net.roarsoftware.lastfm.User;

public class Seed {

	public static void main(String[] args) throws IOException {
		Set<String> seed = new HashSet<String>();
		
		List<Tag> topTags = Tag.getTopTags(LastFMApiCrawlJob.API_KEY);
		for (int i = 0; i < 10; i++) {
			List<Artist> artists = new ArrayList<Artist>(Tag.getTopArtists(topTags.get(i).getName(), LastFMApiCrawlJob.API_KEY));
			for (int j = 0; j < 10; j++) {
				List<User> topFans = new ArrayList<User>(Artist.getTopFans(artists.get(j).getName(), LastFMApiCrawlJob.API_KEY));
				for (int k = 0; k < 10; k++) {
					System.err.println(topTags.get(i).getName() + " -> " + artists.get(j).getName() + " -> " + topFans.get(k).getName());
					seed.add(topFans.get(k).getName());
				}
			}
		}
		
		for (String s : seed) {
			System.out.println(s);
		}
	}
}
