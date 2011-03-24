package br.ufmg.dcc.vod.ncrawler.jobs.youtube.video_api;

import java.io.Serializable;
import java.util.Set;

public class YoutubeVideoDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String videoID;
	private final String author;
	private final String title;
	private final Set<String> tags;
	private final String description;
	private final String category;
	private final long duration;
	private final double latitude;
	private final double longitude;
	private final float avgRating;
	private final int minRating;
	private final int maxRating;
	private final int ratingCount;
	private final long viewCount;
	private final long favCount;

	public YoutubeVideoDAO(String videoID, String author, String title,
			Set<String> tags, String description, String category,
			long duration, double latitude, double longitude, float avgRating,
			int minRating, int maxRating, int ratingCount, long viewCount,
			long favCount) {
				this.videoID = videoID;
				this.author = author;
				this.title = title;
				this.tags = tags;
				this.description = description;
				this.category = category;
				this.duration = duration;
				this.latitude = latitude;
				this.longitude = longitude;
				this.avgRating = avgRating;
				this.minRating = minRating;
				this.maxRating = maxRating;
				this.ratingCount = ratingCount;
				this.viewCount = viewCount;
				this.favCount = favCount;
	}

	public String getVideoID() {
		return videoID;
	}

	public String getAuthor() {
		return author;
	}

	public String getTitle() {
		return title;
	}

	public Set<String> getTags() {
		return tags;
	}

	public String getDescription() {
		return description;
	}

	public String getCategory() {
		return category;
	}

	public long getDuration() {
		return duration;
	}

	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}

	public float getAvgRating() {
		return avgRating;
	}

	public int getMinRating() {
		return minRating;
	}

	public int getMaxRating() {
		return maxRating;
	}

	public int getRatingCount() {
		return ratingCount;
	}

	public long getViewCount() {
		return viewCount;
	}

	public long getFavCount() {
		return favCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((videoID == null) ? 0 : videoID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		YoutubeVideoDAO other = (YoutubeVideoDAO) obj;
		if (videoID == null) {
			if (other.videoID != null)
				return false;
		} else if (!videoID.equals(other.videoID))
			return false;
		return true;
	}
}