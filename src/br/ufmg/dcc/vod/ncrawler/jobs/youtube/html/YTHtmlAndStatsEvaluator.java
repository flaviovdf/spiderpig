package br.ufmg.dcc.vod.ncrawler.jobs.youtube.html;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractEvaluator;

public class YTHtmlAndStatsEvaluator extends AbstractEvaluator<Pair<byte[], byte[]>> {

	private final Collection<String> initialVideos;
	private final File outputFolder;

	public YTHtmlAndStatsEvaluator(Collection<String> initialVideos, File outputFolder) {
		this.initialVideos = initialVideos;
		this.outputFolder = outputFolder;
	}
	
	@Override
	public CrawlJob createJob(String next) {
		return new YTHtmlAndStatsCrawlJob(next);
	}

	@Override
	public Collection<String> getSeeds() {
		return initialVideos;
	}

	@Override
	public Collection<String> realEvaluateAndSave(String collectID,	Pair<byte[], byte[]> collectContent) throws Exception {
		
		Date now = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy_H-mm-ss");
		String htmlFileName = "videoinfo-"+collectID+formatter.format(now);
		String statsFileName = "videostats-"+collectID+formatter.format(now);
		
		BufferedOutputStream htmlFile = null;
		BufferedOutputStream statsFile = null;

		try {
			htmlFile = new BufferedOutputStream(
						new FileOutputStream(
								new File(outputFolder + File.separator + htmlFileName)));
			htmlFile.write(collectContent.first);
		} finally {
			if (htmlFile != null) htmlFile.close();
		}

		try {
			statsFile = new BufferedOutputStream(
						new FileOutputStream(
								new File(outputFolder + File.separator + statsFileName)));
			statsFile.write(collectContent.second);
		} finally {
			if (statsFile != null) statsFile.close();
		}
		
		return null;
	}
}