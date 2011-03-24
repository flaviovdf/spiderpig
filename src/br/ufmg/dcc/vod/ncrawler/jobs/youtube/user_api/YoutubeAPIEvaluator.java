package br.ufmg.dcc.vod.ncrawler.jobs.youtube.user_api;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.MyXStreamer;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractEvaluator;

public class YoutubeAPIEvaluator extends AbstractEvaluator<YoutubeUserDAO> {

	private final Collection<String> initialUsers;
	private final File savePath;
	private final long sleepTime;
	
	public YoutubeAPIEvaluator(Collection<String> initialUsers, File savePath, long sleepTime) {
		this.initialUsers = initialUsers;
		this.savePath = savePath;
		this.sleepTime = sleepTime;
	}

	@Override
	public CrawlJob createJob(String next) {
		return new YoutubeUserAPICrawlJob(next, sleepTime);
	}

	@Override
	public Collection<String> getSeeds() {
		return initialUsers;
	}

	@Override
	public Collection<String> realEvaluateAndSave(String collectID, YoutubeUserDAO collectContent) throws Exception {
		MyXStreamer.getInstance().toXML(collectContent, new File(savePath + File.separator + collectID));
		Set<String> followup = new HashSet<String>();
		//Subscriptions
		Set<String> subscriptions = collectContent.getSubscriptions();
		for (String s : subscriptions) {
			followup.add(s);
		}
		
		//Subscribers
		Set<String> subscribers = collectContent.getSubscribers();
		for (String s : subscribers) {
			followup.add(s);
		}
		
		return followup;
	}
}