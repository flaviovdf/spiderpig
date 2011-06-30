package br.ufmg.dcc.vod.ncrawler.jobs.plus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

public class PlusCrawlJob implements CrawlJob {

	private static final long serialVersionUID = 1L;

	private static final Pattern LINK_PATTERN = Pattern.compile(",\\[\\[,,\"(\\d+)\"\\]\\s*");
	
	private final String profileID;
	private Evaluator eval;

	public PlusCrawlJob(String profileID) {
		this.profileID = profileID;
	}
	
	@Override
	public void collect() {
		try {
			URL circlesUrl = new URL("https://plus.google.com/_/socialgraph/lookup/visible/?o=%5Bnull%2Cnull%2C%22"+ profileID +"%22%5D&_reqid=533684&rt=j");
			PlusDao circlesDao = getHtml(circlesUrl);
			
			URL inUrl = new URL("https://plus.google.com/_/socialgraph/lookup/incoming/?o=%5Bnull%2Cnull%2C%22"+ profileID +"%22%5D&n=1000&_reqid=2133684&rt=j");
			PlusDao inDao = getHtml(inUrl);
			
			eval.evaluteAndSave(profileID, new Pair<PlusDao, PlusDao>(circlesDao, inDao));
		} catch (Exception e) {
			eval.error(profileID, new UnableToCollectException(e.getMessage()));
		}
	}

	private PlusDao getHtml(URL u) throws IOException {
		BufferedReader in = null;
		try {
			URLConnection openConnection = u.openConnection();
			openConnection.setRequestProperty("User-Agent", "Research-Crawler-APIDEVKEY-AI39si59eqKb2OzKrx-4EkV1HkIRJcoYDf_VSKUXZ8AYPtJp-v9abtMYg760MJOqLZs5QIQwW4BpokfNyKKqk1gi52t0qMwJBg");
			
			openConnection.connect();
			
			StringWriter html = new StringWriter();
			PrintWriter writer = new PrintWriter(html);
			writer.println("<crawleduserid = "+ profileID +" >");
			
			Set<String> discoveredIds = new HashSet<String>();
			String inputLine;
			in = new BufferedReader(new InputStreamReader(openConnection.getInputStream()));
			while ((inputLine = in.readLine()) != null) {
				Matcher matcher = LINK_PATTERN.matcher(inputLine);
				if (matcher.matches()) {
					discoveredIds.add(matcher.group(1));
				}
				writer.println(inputLine);
			}
			writer.println("</crawleduserid>");
			writer.close();
			StringBuffer buffer = html.getBuffer();
			buffer.trimToSize();
			
			byte[] content = buffer.toString().getBytes();
			return new PlusDao(content, discoveredIds);
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
		return profileID;
	}

	@Override
	public void setEvaluator(Evaluator eval) {
		this.eval = eval;
	}
}