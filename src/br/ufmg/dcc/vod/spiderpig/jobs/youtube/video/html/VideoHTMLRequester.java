package br.ufmg.dcc.vod.spiderpig.jobs.youtube.video.html;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import br.ufmg.dcc.vod.spiderpig.common.URLGetter;
import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableRequester;
import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResultFactory;
import br.ufmg.dcc.vod.spiderpig.jobs.PayloadsFactory;
import br.ufmg.dcc.vod.spiderpig.jobs.youtube.UnableToCrawlException;
import br.ufmg.dcc.vod.spiderpig.jobs.youtube.YTConstants;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;

import com.google.common.collect.Sets;

public class VideoHTMLRequester implements ConfigurableRequester {
	
	private DefaultHttpClient httpClient;
	private URLGetter urlGetter;

	@Override
	public void configurate(Configuration configuration, 
			ConfigurableBuilder builder) throws BuildException {
		String devKey = configuration.getString(YTConstants.DEV_KEY_V2);
		String appName = configuration.getString(YTConstants.APP_NAME_V2);
		
		this.httpClient = new DefaultHttpClient();
		this.httpClient.getParams().setParameter(
				ClientPNames.COOKIE_POLICY, 
				CookiePolicy.BROWSER_COMPATIBILITY);
		this.urlGetter = new URLGetter("Research-Crawler-APIDEVKEY-" + devKey +
				"APPNAME-" + appName);
	}

	@Override
	public Set<String> getRequiredParameters() {
		return Sets.newHashSet(YTConstants.DEV_KEY_V2, YTConstants.APP_NAME_V2);
	}

	private URI createVideoHTMLUrl(String videoID) 
			throws URISyntaxException {
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").
				setHost("www.youtube.com").
				setPath("/watch").
				setParameter("v", videoID).
				setParameter("gl", "US").
				setParameter("hl", "en");
		return builder.build();
	}

	private URI createVideoStatsUrl(String videoID) 
			throws URISyntaxException {
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").
				setHost("www.youtube.com").
				setPath("/insight_ajax").
				setParameter("action_get_statistics_and_data", "1").
				setParameter("v", videoID).
				setParameter("gl", "US").
				setParameter("hl", "en");
		return builder.build();
	}
	
	private byte[] performRequest(HttpUriRequest request, String videoID) 
			throws IOException {
		String header = "<crawledvideoid = " + videoID + ">";
		String footer = "</crawledvideoid>";
		return this.urlGetter.getHtml(this.httpClient, request, 
				header, footer);
	}
	
	private String getSessionToken(byte[] vidHtml) throws IOException {
		BufferedReader bis = 
				new BufferedReader(new InputStreamReader(
						new ByteArrayInputStream(vidHtml)));
		String inputLine;
		while ((inputLine = bis.readLine()) != null) {
			if (inputLine.contains("insight_ajax")) {
				String[] split = inputLine.split("\"");
				return split[1];
			}
		}
		
		return "";
	}
	
	private byte[] extractStats(byte[] statsHtml) throws IOException {
		BufferedReader bis = 
				new BufferedReader(new InputStreamReader(
						new ByteArrayInputStream(statsHtml)));
		String inputLine;
		while ((inputLine = bis.readLine()) != null) {
			if (inputLine.contains("<graph_data>")) {
				int firstIndexOf = inputLine.indexOf("{");
				int lastIndexOf = inputLine.lastIndexOf("}");
				String jsonData = 
						inputLine.substring(firstIndexOf, lastIndexOf + 1);
				return jsonData.getBytes();
			}
		}
		
		return "".getBytes();
	}
	
	@Override
	public CrawlResult performRequest(CrawlID crawlID) {
		CrawlResultFactory resultBuilder = new CrawlResultFactory(crawlID);
		PayloadsFactory payloadBuilder = new PayloadsFactory();
		String id = crawlID.getId();
		
		try {
			HttpGet getMethod = new HttpGet(createVideoHTMLUrl(id));
			byte[] vidHtml = performRequest(getMethod, id);
			String ajaxToken = getSessionToken(vidHtml);
			
			HttpPost postMethod = new HttpPost(createVideoStatsUrl(id));
			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
			formParams.add(new BasicNameValuePair("session_token", 
					ajaxToken));
			UrlEncodedFormEntity entity = 
					new UrlEncodedFormEntity(formParams, "UTF-8");
			postMethod.setEntity(entity);
			byte[] statsHtml = performRequest(postMethod, id);
			byte[] statsJson = extractStats(statsHtml);
			payloadBuilder.addPayload(crawlID, statsJson, "-stats.json");
			return resultBuilder.buildOK(payloadBuilder.build(), null);
		} catch (IOException | URISyntaxException e) {
			UnableToCrawlException cause = new UnableToCrawlException(e);
			return resultBuilder.buildNonQuotaError(cause);
		}
	}
}