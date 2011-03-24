package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.user_apihtml;

import java.io.File;
import java.util.Collection;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.MyXStreamer;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractEvaluator;

public class LastFMAPIEvaluator extends AbstractEvaluator<LastFMUserDAO> {

	private final List<String> seeds;
	private final File saveFolder;
	private final long sleepTime;

	public LastFMAPIEvaluator(List<String> seeds, File saveFolder, long sleepTime) {
		this.seeds = seeds;
		this.saveFolder = saveFolder;
		this.sleepTime = sleepTime;
	}

	@Override
	public Collection<String> realEvaluateAndSave(String collectID, LastFMUserDAO collectContent) throws Exception {
		MyXStreamer.getInstance().toXML(collectContent, new File(saveFolder.getAbsolutePath() + File.separator + collectID));
		return collectContent.getFriendNames();
	}

	@Override
	public CrawlJob createJob(String next) {
		return new LastFMApiCrawlJob(next, sleepTime);
	}

	@Override
	public Collection<String> getSeeds() {
		return seeds;
	}
}
