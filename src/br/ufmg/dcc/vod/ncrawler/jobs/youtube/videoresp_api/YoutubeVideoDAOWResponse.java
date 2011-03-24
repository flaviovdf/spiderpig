package br.ufmg.dcc.vod.ncrawler.jobs.youtube.videoresp_api;

import java.util.Set;

import br.ufmg.dcc.vod.ncrawler.jobs.youtube.video_api.YoutubeVideoDAO;

public class YoutubeVideoDAOWResponse extends YoutubeVideoDAO {

	private static final long serialVersionUID = 1L;
	private final Set<String> responses;

	public YoutubeVideoDAOWResponse(String videoID, String author, String title,
			Set<String> tags, String description, String category,
			long duration, double latitude, double longitude, float avgRating,
			int minRating, int maxRating, int ratingCount, long viewCount,
			long favCount, Set<String> responses) {
		
		super(videoID, author, title,
			tags, description, category,
			duration, latitude, longitude, avgRating,
			minRating, maxRating, ratingCount, viewCount,
			favCount);
		this.responses = responses;
	}
	
	public Set<String> getResponses() {
		return responses;
	}
}
