package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.user_apihtml;

import java.io.Serializable;
import java.util.Collection;

public class LastFMUserDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String userID;
	private final Collection<String> friends;
	private final Collection<LastFMTagDAO> toptags;
	private final Collection<String> loved;
	private final Collection<LastFMArtistDAO> artists;
	private final String fullName;
	private final int age;
	private final String gender;
	private final String country;
	private final String lastSeen;
	private final String accountAge;
	private final String collectTime;


	public LastFMUserDAO(String userID, Collection<String> friendNames,
			Collection<LastFMArtistDAO> artists, Collection<String> loved, Collection<LastFMTagDAO> discoverTagDAO, 
			String fullName, int age, String gender, String country, String lastSeen, String accountAge, String collectTime) {
				this.userID = userID;
				this.friends = friendNames;
				this.artists = artists;
				this.loved = loved;
				this.toptags = discoverTagDAO;
				this.fullName = fullName;
				this.age = age;
				this.gender = gender;
				this.country = country;
				this.lastSeen = lastSeen;
				this.accountAge = accountAge;
				this.collectTime = collectTime;
	}

	public String getUserID() {
		return userID;
	}
	
	public Collection<LastFMTagDAO> getTopTags() {
		return toptags;
	}

	public String getFullName() {
		return fullName;
	}

	public int getAge() {
		return age;
	}

	public String getGender() {
		return gender;
	}

	public String getCountry() {
		return country;
	}

	public String getLastSeen() {
		return lastSeen;
	}

	public String getAccountAge() {
		return accountAge;
	}

	public String getCollectTime() {
		return collectTime;
	}

	public Collection<String> getFriendNames() {
		return friends;
	}

	public Collection<String> getLoved() {
		return loved;
	}

	public Collection<LastFMArtistDAO> getArtists() {
		return artists;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
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
		LastFMUserDAO other = (LastFMUserDAO) obj;
		if (userID == null) {
			if (other.userID != null)
				return false;
		} else if (!userID.equals(other.userID))
			return false;
		return true;
	}

}
