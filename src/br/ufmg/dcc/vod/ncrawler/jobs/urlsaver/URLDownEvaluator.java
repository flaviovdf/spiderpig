package br.ufmg.dcc.vod.ncrawler.jobs.urlsaver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;

import org.apache.commons.codec.net.URLCodec;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractEvaluator;

public class URLDownEvaluator extends AbstractEvaluator<byte[]> {


	private final Collection<String> urls;
	private final File savePath;

	public URLDownEvaluator(Collection<String> urls,  File savePath) {
		this.urls = urls;
		this.savePath = savePath;
	}
	
	@Override
	public CrawlJob createJob(String next) {
		return new URLDownCrawlJob(next);
	}

	@Override
	public Collection<String> getSeeds() {
		return urls;
	}

	@Override
	public Collection<String> realEvaluateAndSave(String collectID, byte[] collectContent) throws Exception {
		File output = new File(savePath + File.separator + new URLCodec().encode(collectID));
		BufferedOutputStream w = null;
		try {
			w = new BufferedOutputStream(new FileOutputStream(output));
			w.write(collectContent);
			w.close();
		} finally {
			if (w != null) w.close();
		}
		return null;
	}
}
