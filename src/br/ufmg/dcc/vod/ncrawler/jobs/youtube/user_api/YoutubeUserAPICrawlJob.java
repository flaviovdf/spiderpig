package br.ufmg.dcc.vod.ncrawler.jobs.youtube.user_api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.Link;
import com.google.gdata.data.youtube.FriendEntry;
import com.google.gdata.data.youtube.FriendFeed;
import com.google.gdata.data.youtube.SubscriptionEntry;
import com.google.gdata.data.youtube.SubscriptionFeed;
import com.google.gdata.data.youtube.UserProfileEntry;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YtUserProfileStatistics;
import com.google.gdata.util.ServiceException;

public class YoutubeUserAPICrawlJob implements CrawlJob {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(YoutubeAPIEvaluator.class);
	
	private Evaluator e;
	private final String userID;
	private final long sleepTime;

	private final Pattern NEXT_PATTERN = Pattern.compile("(\\s+&nbsp;<a href=\")(.*?)(\"\\s*>\\s*Next.*)");
	private final Pattern RELATION_PATTERN = Pattern.compile("(\\s*<a href=\"/user/)(.*?)(\"\\s+onmousedown=\"trackEvent\\('ChannelPage'.*)");
	private final Pattern ERROR_PATTERN = Pattern.compile("\\s*<input type=\"hidden\" name=\"challenge_enc\" value=\".*");
	
	public YoutubeUserAPICrawlJob(String userID, long sleepTime) {
		this.userID = userID;
		this.sleepTime = sleepTime;
	}
	
	@Override
	public void collect() {
		try {
			LOG.info("Collecting: " + userID);
			
			YouTubeService service = new YouTubeService("ytapi-FlavioVinicius-DataCollector-si5mgkd4-0", "AI39si59eqKb2OzKrx-4EkV1HkIRJcoYDf_VSKUXZ8AYPtJp-v9abtMYg760MJOqLZs5QIQwW4BpokfNyKKqk1gi52t0qMwJBg");
			UserProfileEntry profileEntry = service.getEntry(new URL("http://gdata.youtube.com/feeds/api/users/" + userID), UserProfileEntry.class);
			
			String username = profileEntry.getUsername();
			int age = profileEntry.getAge() == null ? -1 : profileEntry.getAge();
			String gender = profileEntry.getGender() == null ? null : profileEntry.getGender().getId();
			String relationship = profileEntry.getRelationship() == null ? null : profileEntry.getRelationship().name();
			String books = profileEntry.getBooks();
			String company = profileEntry.getCompany();
			String aboutMe = profileEntry.getAboutMe();
			String hobbies = profileEntry.getHobbies();
			String hometown = profileEntry.getHometown();
			String location = profileEntry.getLocation();
			String movies = profileEntry.getMovies();
			String music = profileEntry.getMusic();
			String occupation = profileEntry.getOccupation();
			String school = profileEntry.getSchool();
			String channelType = profileEntry.getChannelType();
			
			YtUserProfileStatistics stats = profileEntry.getStatistics();
			Date lastWebAccess = null;
			long videoWatchCount = -1;
			long viewCount = -1;
			if(stats != null) {
				lastWebAccess = stats.getLastWebAccess() == null ? null : new Date(stats.getLastWebAccess().getValue());
				videoWatchCount = stats.getVideoWatchCount();
				viewCount = stats.getViewCount();
			}
	
			Thread.sleep(sleepTime);
			
			Set<String> uploads = new HashSet<String>();
			try {
				Link uploadsFeedLink = profileEntry.getUploadsFeedLink();
				while (uploadsFeedLink != null) {
					String href = uploadsFeedLink.getHref();
					VideoFeed upsFeed = service.getFeed(new URL(href), VideoFeed.class);
					for (VideoEntry ve : upsFeed.getEntries()) {
						String[] split = ve.getId().split("\\/");
						String id = split[split.length - 1];
						uploads.add(id);
					}
					uploadsFeedLink = upsFeed.getLink("next", "application/atom+xml");
					
					Thread.sleep(sleepTime);
				}
			} catch (ServiceException e) {
				LOG.warn("Unable to collect every upload for user " + userID, e);
			}

			Set<String> friends = new HashSet<String>();
			try {
				Link friendsFeedLink = profileEntry.getContactsFeedLink();
				while (friendsFeedLink != null) {
					String href = friendsFeedLink.getHref();
					FriendFeed friendFeed = service.getFeed(new URL(href), FriendFeed.class);
					for (FriendEntry fe : friendFeed.getEntries()) {
						friends.add(fe.getUsername());
					}
					friendsFeedLink = friendFeed.getLink("next", "application/atom+xml");
					
					Thread.sleep(sleepTime);
				}
			} catch (ServiceException e) {
				LOG.warn("Unable to collect every friend for user " + userID, e);
			}

			Set<String> subscriptions = new HashSet<String>();
			try {
				Link subscriptionsFeedLink = profileEntry.getSubscriptionsFeedLink();
				while (subscriptionsFeedLink != null) {
					String href = subscriptionsFeedLink.getHref();
					SubscriptionFeed subscriptionFeed = service.getFeed(new URL(href), SubscriptionFeed.class);
					for (SubscriptionEntry se : subscriptionFeed.getEntries()) {
						switch (se.getSubscriptionType()) {
							case CHANNEL:
								subscriptions.add(se.getUsername());
								break;
							default:
								continue;
						}
					}
					
					subscriptionsFeedLink = subscriptionFeed.getLink("next", "application/atom+xml");
					
					Thread.sleep(sleepTime);
				}
			} catch (ServiceException e) {
				LOG.warn("Unable to collect every subscriptions for user " + userID, e);
			}
			
			Set<String> subscribers = new HashSet<String>();
			try {
				subscribers.addAll(discoverSubscribers(userID));
			} catch (Exception e) {
				LOG.warn("Unable to collect every subscriber for user " + userID, e);
			}
			
			YoutubeUserDAO collectContent = new YoutubeUserDAO(userID, username, age, gender, aboutMe, relationship, books, company, hobbies, hometown, location, movies, music, occupation, school, channelType, uploads, subscriptions, subscribers, friends, viewCount, videoWatchCount, lastWebAccess);
			LOG.info("Done, sending... " + userID);
			e.evaluteAndSave(userID, collectContent);
		} catch (Exception ec ) {
			LOG.error("Done with errors, sending... " + userID, ec);
			e.error(userID, new UnableToCollectException(ec.getMessage()));
		}
	}

	private Set<String> discoverSubscribers(String collectID) throws Exception {
		Set<String> rv = new HashSet<String>();
		
		String followLink = "http://www.youtube.com/profile?user=" + collectID + "&view=subscribers&gl=US&hl=en";
		String lastLink = null;
		
		do {
			lastLink = followLink;
			BufferedReader in = null;
			
			try {
				URL u = new URL(followLink);
				URLConnection connection = u.openConnection();
				connection.setRequestProperty("User-Agent", "Research-Crawler-APIDEVKEY-AI39si59eqKb2OzKrx-4EkV1HkIRJcoYDf_VSKUXZ8AYPtJp-v9abtMYg760MJOqLZs5QIQwW4BpokfNyKKqk1gi52t0qMwJBg");
				
				connection.connect();
				
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					Matcher matcher = NEXT_PATTERN.matcher(inputLine);
					if (matcher.matches() && inputLine.contains("subscribers")) {
						followLink = "http://www.youtube.com/" + matcher.group(2) + "&gl=US&hl=en";
					}
					
					matcher = RELATION_PATTERN.matcher(inputLine);
					if (matcher.matches()) {
						rv.add(matcher.group(2));
					}
					
					matcher = ERROR_PATTERN.matcher(inputLine);
					if (matcher.matches()) {
						throw new YTErrorPageException();
					}
				}
			} finally {
				if (in != null) in.close();
			}
			
			Thread.sleep(sleepTime);
		} while (!followLink.equals(lastLink));
		
		return rv;
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