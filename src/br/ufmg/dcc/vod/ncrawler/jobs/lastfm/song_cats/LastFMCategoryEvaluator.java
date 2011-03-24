package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.song_cats;

import java.io.File;
import java.util.Collection;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.MyXStreamer;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractEvaluator;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api.ArtistSongUtils;

public class LastFMCategoryEvaluator  extends AbstractEvaluator<List<Pair<String, String>>> {

	private final Collection<String> seeds;
	private final File outDir;

	public LastFMCategoryEvaluator(Collection<String> seeds, File outDir) {
		this.seeds = seeds;
		this.outDir = outDir;
	}
	
	@Override
	public CrawlJob createJob(String next) {
		return new LastFMCategoryCrawlJob(next);
	}

	@Override
	public Collection<String> getSeeds() {
		return seeds;
	}

	@Override
	public Collection<String> realEvaluateAndSave(String collectID, List<Pair<String, String>> collectContent) throws Exception {
        if (!collectContent.isEmpty()) {
    		String encode = ArtistSongUtils.encode(collectID);
	    	File f = new File(outDir.getAbsoluteFile() + File.separator + encode);
		    MyXStreamer.getInstance().toXML(collectContent, f);
        }
		
		return null;
	}
}
