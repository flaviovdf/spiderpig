package br.ufmg.dcc.vod.ncrawler.jobs.plus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractEvaluator;

public class PlusEvaluator extends AbstractEvaluator<Pair<PlusDao, PlusDao>> {

	private final Collection<String> initialIds;
	private final File outputFolder;

	public PlusEvaluator(Collection<String> initialIds, File outputFolder) {
		this.initialIds = initialIds;
		this.outputFolder = outputFolder;
	}
	
	@Override
	public CrawlJob createJob(String next) {
		return new PlusCrawlJob(next);
	}

	@Override
	public Collection<String> getSeeds() {
		return initialIds;
	}

	@Override
	public Collection<String> realEvaluateAndSave(String collectID,	Pair<PlusDao, PlusDao> collectContent) throws Exception {
		
		Date now = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy_H-mm-ss");
		String circlesFileName = "circles-"+collectID+formatter.format(now);
		String inlinksFileName = "inlinks-"+collectID+formatter.format(now);
		
		BufferedOutputStream htmlFile = null;
		BufferedOutputStream statsFile = null;

		try {
			htmlFile = new BufferedOutputStream(
						new FileOutputStream(
								new File(outputFolder + File.separator + circlesFileName)));
			htmlFile.write(collectContent.first.getContent());
		} finally {
			if (htmlFile != null) htmlFile.close();
		}

		try {
			statsFile = new BufferedOutputStream(
						new FileOutputStream(
								new File(outputFolder + File.separator + inlinksFileName)));
			statsFile.write(collectContent.second.getContent());
		} finally {
			if (statsFile != null) statsFile.close();
		}
		
		Set<String> rv = new HashSet<String>();
		rv.addAll(collectContent.first.getDiscoveredIds());
		rv.addAll(collectContent.second.getDiscoveredIds());
		return rv;
	}
}