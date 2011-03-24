package br.ufmg.dcc.vod.ncrawler.jobs.urlsaver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

public class URLDownCrawlJob implements CrawlJob {

	private final String url;
	private Evaluator eval;

	public URLDownCrawlJob(String url) {
		this.url = url;
	}
	
	@Override
	public void collect() {
		BufferedReader in = null;
		try {
			URL u = new URL(url);
			URLConnection openConnection = u.openConnection();
			
			//FIXME: Remove this!
			openConnection.setRequestProperty("User-Agent", "Research-Crawler-APIDEVKEY-AI39si59eqKb2OzKrx-4EkV1HkIRJcoYDf_VSKUXZ8AYPtJp-v9abtMYg760MJOqLZs5QIQwW4BpokfNyKKqk1gi52t0qMwJBg");
			
			openConnection.connect();
			
			StringWriter html = new StringWriter();
			PrintWriter writer = new PrintWriter(html);
			
			writer.println("<!-- URL: " + url + " -->");
			
			String inputLine;
			in = new BufferedReader(new InputStreamReader(openConnection.getInputStream()));
			while ((inputLine = in.readLine()) != null) {
				writer.println(inputLine);
			}
			writer.close();
			
			html.getBuffer().trimToSize();
			this.eval.evaluteAndSave(url, html.getBuffer().toString().getBytes());
		} catch (Exception e) {
			this.eval.error(url, new UnableToCollectException(e.getMessage()));
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}		
	}

	@Override
	public String getID() {
		return url;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setEvaluator(Evaluator eval) {
		this.eval = eval;
	}
}
