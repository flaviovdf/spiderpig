package br.ufmg.dcc.vod.ncrawler.jobs.youtube.video_api;

import java.io.File;
import java.util.Collection;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.MyXStreamer;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractEvaluator;

public class YoutubeVideoAPIEvaluator extends AbstractEvaluator<YoutubeVideoDAO> {

	private Collection<String> initialVideos;
	private File savePath;

	public YoutubeVideoAPIEvaluator(Collection<String> initialVideos, File savePath) {
		this.initialVideos = initialVideos;
		this.savePath = savePath;
	}
	
	@Override
	public CrawlJob createJob(String next) {
		return new YoutubeAPIVideoCrawlJob(next);
	}

	@Override
	public Collection<String> getSeeds() {
		return initialVideos;
	}

	@Override
	public Collection<String> realEvaluateAndSave(String collectID, YoutubeVideoDAO collectContent) throws Exception {
		MyXStreamer.getInstance().toXML(collectContent, new File(savePath + File.separator + collectID));
		return null;
	}
}
