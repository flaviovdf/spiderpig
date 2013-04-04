package br.ufmg.dcc.vod.spiderpig.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class URLGetter {

	public static final String UTF8 = "utf-8";
	public static final Charset UTF8_CS = Charset.forName(UTF8);
	public static final String NL = System.lineSeparator();
	
	private Map<String, String> requestProperties;
	private Charset charSet;
	
	public URLGetter() {
		this.requestProperties = new HashMap<>();
		this.requestProperties.put("Accept-Charset", UTF8);
		this.requestProperties.put("Accept-Language", "en-US,en");
        this.charSet = Charset.forName(UTF8);
	}

	public URLGetter(String charset, String lang) {
		this.requestProperties = new HashMap<>();
		this.requestProperties.put("Accept-Charset", charset);
		this.requestProperties.put("Accept-Language", lang);
		this.charSet = Charset.forName(charset);
	}
	
	public byte[] getHtml(URL u, String header, String footer) 
			throws Exception {
		BufferedReader in = null;
		try {
			URLConnection openConnection = u.openConnection();
			
			for (Entry<String, String> e : requestProperties.entrySet())
				openConnection.setRequestProperty(e.getKey(), e.getValue());
			
			openConnection.connect();
			
			StringBuilder html = new StringBuilder();
			
			html.append(header);
			html.append(NL);
			
			String inputLine;
			in = new BufferedReader(new InputStreamReader(
					openConnection.getInputStream()));
			while ((inputLine = in.readLine()) != null) {
				html.append(inputLine);
				html.append(NL);
			}
			
			html.append(footer);
			html.append(NL);
			html.trimToSize();
			return  html.toString().getBytes(this.charSet);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
	
	public void setProperty(String key, String value) {
		this.requestProperties.put(key, value);
	}
}
