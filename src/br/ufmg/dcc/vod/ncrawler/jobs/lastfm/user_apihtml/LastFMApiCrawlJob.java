package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.user_apihtml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.roarsoftware.lastfm.Caller;
import net.roarsoftware.lastfm.Result;
import net.roarsoftware.lastfm.Track;
import net.roarsoftware.lastfm.User;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

public class LastFMApiCrawlJob implements CrawlJob {

	private static final Logger LOG = Logger.getLogger(LastFMApiCrawlJob.class);
	private static final long serialVersionUID = 1L;
	
	//For HTML Requests
	private static final String USER_AGENT_VALUE = "Research-Crawler-APIDEVKEY-c86a6f99618d3dbfcf167366be991f3b";
	private static final String USER_AGENT = "User-Agent";
	
	//For APT Limits
	private static final int LIMIT = 1000000000;
	
	//API Stuff
	public static final String API_KEY     = "c86a6f99618d3dbfcf167366be991f3b";
	public static final String API_SECRET  = "6fb4fdae8ddcfa6d7a70024aec7a0e42";
	public static final String SESSION_KEY = "fe1acda9911e017979532610a88c0db8";
	
	//Patterns for tags
	private static final Pattern SONG_PATTERN_FOR_TAG = Pattern.compile("(\\s+<a href=\")(.*?)(</a> – <a href=\")(/music/.*?/_/.*?)(\")(\"*.*?\"*)( class=\"primary\">.*)");
	private static final Pattern ALBUM_PATTERN_FOR_TAG = Pattern.compile("(\\s+<a href=\")(/music/.*?)(\")(\"*.*?\"*)( class=\"primary\">\\s+<span class=\"albumCover coverSmall resImage\">.*)");
	private static final Pattern ARTIST_PATTERN_FOR_TAG = Pattern.compile("(\\s+<a href=\")(/music/.*?)(\")(\"*.*?\"*)( class=\"primary\".*?)(</a>.*)");
	
	//Patterns for plays
    private static final Pattern ARTIST_PATTERN_FOR_PLAYS = Pattern.compile("(\\s+<a href=\")(/music/.*?)(\")(\"*.*?\"*)(><span class=\"pictureFrame\"><span class=\"image\">.*class=\"plays\" rel=\"nofollow\">\\()(.*)(\\&nbsp;.*)");
    private static final Pattern NUM_PAGES_FOR_PLAYS = Pattern.compile("(\\s+Page <span class=\"pagenumber\">\\d+</span> of )(\\d+)");
    
    //Patterns for user data
    private static final Pattern BASIC_INFO = Pattern.compile("(\\s+<p class=\"userInfo adr\"><strong class=\"fn\">)(.*?)(</strong>)(.*?)(<span class=\"country-name\">)(.*?)(</span>.*?<small class=\"userLastseen\">\\s*Last seen:\\s*)(.*)(</small></p>.*)");
    private static final Pattern ACCOUNT_AGE = Pattern.compile("(.*?<small>since )(.*)(</small>.*)");

    
	private final String userID;
	private final long sleepTime;
	private Evaluator e;

	public LastFMApiCrawlJob(String userID, long sleepTime) {
		this.userID = userID;
		this.sleepTime = sleepTime;
	}
	
	@Override
	public void collect() {
		LOG.info("Collecting user: " + userID);
		boolean allFailed = true;

		//Basic Data
		String fullName = "";
		int age = -1;
		String gender = "";
		String country = "";
		String lastSeen = "";
		String accountAge = "";
		
		try {
			List basicData = getBasicData();
			fullName = (String) basicData.get(0);
			age = (Integer) basicData.get(1);
			gender = (String) basicData.get(2);
			country = (String) basicData.get(3);
			lastSeen = (String) basicData.get(4);
			accountAge = (String) basicData.get(5);
			
			allFailed = false;
		} catch (Exception ioe) {
			LOG.warn("Unable to collect basic info for user " + userID, ioe);
		}
		
		//Friends
		Collection<User> friends = User.getFriends(userID, false, LIMIT, API_KEY);
		Result lastResult = Caller.getInstance().getLastResult();

		ArrayList<String> friendNames = new ArrayList<String>();
		if (lastResult.isSuccessful()) {
			allFailed = false;
			
			for (User u : friends) {
				friendNames.add(u.getName());
			}
		} else {
			LOG.warn("Unable to collect friends for user " + userID);
		}
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e1) {
		}

		//Loved Tracks
		
		Collection<Track> lovedTracks = User.getLovedTracks(userID, API_KEY);
		lastResult = Caller.getInstance().getLastResult();
		
		ArrayList<String> loved = new ArrayList<String>();
		if (lastResult.isSuccessful()) {
			allFailed = false;
			
			for (Track t : lovedTracks) {
				String url = t.getUrl().replaceAll("www\\.last\\.fm", "");
				loved.add(url);
			}
			
		} else {
			LOG.warn("Unable to collect loved tracks for user " + userID);
		}
		
		//Artists Listened
		
		Collection<LastFMArtistDAO> artists = new HashSet<LastFMArtistDAO>();
		try {
			artists = discoverArtists();
			allFailed = false;
		} catch (Exception ioe) {
			LOG.warn("Unable to collected artist data for user " + userID, ioe);
		}
		
		//Tags
		
		Collection<String> topTags = new HashSet<String>();
		lastResult = Caller.getInstance().getLastResult();
		if (lastResult.isSuccessful()) {
			allFailed = false;
			topTags = User.getTopTags(userID, LIMIT, API_KEY);
		} else {
			LOG.warn("Unable to collect tags for user " + userID);
		}
		
		Collection<LastFMTagDAO> discoverTagDAO = new ArrayList<LastFMTagDAO>();
		try {
			discoverTagDAO = discoverTagDAO(topTags);
			allFailed = false;
		} catch (Exception ioe) {
			LOG.warn("Unable to collected specific tag data for user " + userID, ioe);
		}
		
		if (!allFailed) {
			LastFMUserDAO lfmu = new LastFMUserDAO(userID, friendNames, artists, loved, discoverTagDAO,
					fullName, age, gender, country, lastSeen, accountAge, new Date().toString());
			
			LOG.info("Info collected user: " + userID);
			e.evaluteAndSave(userID, lfmu);
		} else {
			LOG.warn("Unable to collect user: " + userID);
			e.error(userID, new UnableToCollectException("Unable to collect user"));
		}
	}

	private List getBasicData() throws IOException {
		List rv = new ArrayList();
		
		String u = getUserURL();
		URL url = new URL(u);
		
		URLConnection connection = url.openConnection();
		connection.setRequestProperty(USER_AGENT, USER_AGENT_VALUE);
		
		connection.connect();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				Matcher infoMatcher = BASIC_INFO.matcher(inputLine);
				Matcher ageMatcher = ACCOUNT_AGE.matcher(inputLine);
				
				if (infoMatcher.matches()) {
					String fullName = (infoMatcher.group(2));
					String[] split = infoMatcher.group(4).split(",");
					int age = Integer.parseInt(split[1].trim());
					String gender = split[2].trim();
					String country = (infoMatcher.group(6));
					String lastSeen = (infoMatcher.group(8));
					
					rv.add(fullName);
					rv.add(age);
					rv.add(gender);
					rv.add(country);
					rv.add(lastSeen);
				}
				
				if (ageMatcher.matches()) {
					String accountAge = ageMatcher.group(2);
					rv.add(accountAge);
				}
			}
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}
		
		return rv;
	}

	private String getUserURL() throws UnsupportedEncodingException {
		StringBuffer userURL = new StringBuffer("http://www.last.fm/user/");
		userURL.append(URLEncoder.encode(userID, "UTF8"));
		return userURL.toString();
	}

	private Collection<LastFMArtistDAO> discoverArtists() throws IOException {
		ArrayList<LastFMArtistDAO> rv = new ArrayList<LastFMArtistDAO>();
		
		int pNum = 1;
		int limit = 1;
		boolean continueCollecting = true;
		do {
			
			String u = getPlaylistURL(pNum);
			URL url = new URL(u);
			
			URLConnection connection = url.openConnection();
			connection.setRequestProperty(USER_AGENT, USER_AGENT_VALUE);
			
			connection.connect();
			
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					Matcher artistMatcher = ARTIST_PATTERN_FOR_PLAYS.matcher(inputLine);
					
					if (limit == 1) {
						Matcher limitMatcher = NUM_PAGES_FOR_PLAYS.matcher(inputLine);
						if (limitMatcher.matches()) {
							limit = Integer.parseInt(limitMatcher.group(2));
						}
					}
					
					if (artistMatcher.matches()) {
						String artistUrl = artistMatcher.group(2);
						Integer playCount = Integer.parseInt(artistMatcher.group(6).replace(",", ""));
						rv.add(new LastFMArtistDAO(artistUrl, playCount));
					}
				}
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
					}
			}
			
			pNum++;
			continueCollecting = pNum <= limit;
		} while (continueCollecting);
		
		return rv;
	}

	private String getPlaylistURL(int pNum) throws UnsupportedEncodingException {
		StringBuffer playListURL = new StringBuffer("http://www.last.fm/user/");
		playListURL.append(URLEncoder.encode(userID, "UTF8"));
		playListURL.append("/library?sortOrder=desc&sortBy=plays");
		playListURL.append("&page=");
		playListURL.append(pNum);
		
		return playListURL.toString();
	}
	
	private Collection<LastFMTagDAO> discoverTagDAO(Collection<String> topTags) throws IOException {
		ArrayList<LastFMTagDAO> rv = new ArrayList<LastFMTagDAO>();
		
		for (String tag : topTags) {
			boolean continueCollecting = true;
			
			Set<String> artists = new HashSet<String>();
			Set<String> songs = new HashSet<String>();
			Set<String> albums = new HashSet<String>();
			
			int pNum = 1;
			while (continueCollecting) {
				int previousSize = artists.size() + songs.size() + albums.size();
				
				String u = getTagURL(pNum, tag);
				URL url = new URL(u);
				
				URLConnection connection = url.openConnection();
				connection.setRequestProperty(USER_AGENT, USER_AGENT_VALUE);
				
				connection.connect();
				
				BufferedReader in = null;
				try {
					in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						Matcher artistMatcher = ARTIST_PATTERN_FOR_TAG.matcher(inputLine);
						Matcher songMatcher = SONG_PATTERN_FOR_TAG.matcher(inputLine);
						Matcher albumMatcher = ALBUM_PATTERN_FOR_TAG.matcher(inputLine);
						
						if (songMatcher.matches()) {
							String song = songMatcher.group(4);
							songs.add(song);
						} else if (albumMatcher.matches()) {
							String album = albumMatcher.group(2);
							albums.add(album);
						} else if (artistMatcher.matches()) {
							String artist = artistMatcher.group(2);
							artists.add(artist);
						}
					}
				} finally {
					if (in != null)
						try {
							in.close();
						} catch (IOException e) {
						}
				}
				
				int newSize = artists.size() + songs.size() + albums.size();
				continueCollecting = previousSize != newSize;
				pNum++;
			}
			
			rv.add(new LastFMTagDAO(tag, artists, albums, songs));
		}
		
		return rv;
	}

	private String getTagURL(int pNum, String tag) throws UnsupportedEncodingException {
		StringBuffer tagURL = new StringBuffer("http://www.last.fm/user/");
		tagURL.append(URLEncoder.encode(userID, "UTF8"));
		tagURL.append("/library/tags?tag=");
		tagURL.append(URLEncoder.encode(tag, "UTF8"));
		tagURL.append("&page=");
		tagURL.append(pNum);
		
		return tagURL.toString();
	}
	
	@Override
	public void setEvaluator(Evaluator e) {
		this.e = e;
	}

	@Override
	public String getID() {
		return userID;
	}
}
