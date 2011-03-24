package br.ufmg.dcc.vod.ncrawler.jobs.youtube.videoresp_api;

import java.io.File;
import java.util.Collection;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.MyXStreamer;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractEvaluator;

public class VideoResponseEvaluator extends AbstractEvaluator<YoutubeVideoDAOWResponse> {

	private final List<String> seeds;
	private final File saveFolder;

	public VideoResponseEvaluator(List<String> seeds, File saveFolder) {
		this.seeds = seeds;
		this.saveFolder = saveFolder;
	}
	
	@Override
	public CrawlJob createJob(String next) {
		return new YoutubeAPIVidResponseJob(next);
	}

	@Override
	public Collection<String> getSeeds() {
		return seeds;
	}

	@Override
	public Collection<String> realEvaluateAndSave(String collectID,
			YoutubeVideoDAOWResponse collectContent) throws Exception {
		MyXStreamer.getInstance().toXML(collectContent, new File(saveFolder + File.separator + collectID));
		return collectContent.getResponses();
	}
}
