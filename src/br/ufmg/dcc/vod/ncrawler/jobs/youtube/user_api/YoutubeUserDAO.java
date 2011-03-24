package br.ufmg.dcc.vod.ncrawler.jobs.youtube.user_api;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class YoutubeUserDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String userID;
	private final String username;
	private final int age;
	private final String gender;
	private final String aboutMe;
	private final String relationship;
	private final String company;
	private final String hobbies;
	private final String hometown;
	private final String location;
	private final String books;
	private final String channelType;
	private final Set<String> uploads;
	private final Set<String> subscriptions;
	private final long videoWatchCount;
	private final long viewCount;
	private final Date lastWebAccess;
	private final Set<String> friends;
	private final Set<String> subscribers;

	public YoutubeUserDAO(String userID, String username, int age,
			String gender, String aboutMe, String relationship, String books,
			String company, String hobbies, String hometown, String location,
			String movies, String music, String occupation, String school,
			String channelType, Set<String> uploads,
			Set<String> subscriptions, Set<String> subscribers, Set<String> friends, long viewCount, long videoWatchCount, Date lastWebAccess) {
				this.userID = userID;
				this.username = username;
				this.age = age;
				this.gender = gender;
				this.aboutMe = aboutMe;
				this.relationship = relationship;
				this.books = books;
				this.company = company;
				this.hobbies = hobbies;
				this.hometown = hometown;
				this.location = location;
				this.channelType = channelType;
				this.uploads = uploads;
				this.subscriptions = subscriptions;
				this.subscribers = subscribers;
				this.friends = friends;
				this.viewCount = viewCount;
				this.videoWatchCount = videoWatchCount;
				this.lastWebAccess = lastWebAccess;
	}

	public String getUserID() {
		return userID;
	}

	public String getUsername() {
		return username;
	}

	public int getAge() {
		return age;
	}

	public String getGender() {
		return gender;
	}

	public String getAboutMe() {
		return aboutMe;
	}

	public String getRelationship() {
		return relationship;
	}

	public String getCompany() {
		return company;
	}

	public String getHobbies() {
		return hobbies;
	}

	public String getHometown() {
		return hometown;
	}

	public String getLocation() {
		return location;
	}

	public String getBooks() {
		return books;
	}

	public String getChannelType() {
		return channelType;
	}

	public Set<String> getUploads() {
		return uploads;
	}

	public Set<String> getSubscriptions() {
		return subscriptions;
	}

	public long getVideoWatchCount() {
		return videoWatchCount;
	}

	public long getViewCount() {
		return viewCount;
	}

	public Date getLastWebAccess() {
		return lastWebAccess;
	}
	
	public Set<String> getFriends() {
		return friends;
	}

	public Set<String> getSubscribers() {
		return subscribers;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
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
		YoutubeUserDAO other = (YoutubeUserDAO) obj;
		if (userID == null) {
			if (other.userID != null)
				return false;
		} else if (!userID.equals(other.userID))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
}