package br.ufmg.dcc.vod.ncrawler.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;

public class NetIFRoundRobin {

	private static NetIFRoundRobin instance;
	
	private ArrayList<InetAddress> ifs;
	private int current;

	private NetIFRoundRobin() {
		this.current = 0;
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			ifs = new ArrayList<InetAddress>();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface next = networkInterfaces.nextElement();
				if (!next.isLoopback()) {
					Enumeration<InetAddress> inetAddresses = next.getInetAddresses();
					while (inetAddresses.hasMoreElements()) {
						InetAddress nextElement = inetAddresses.nextElement();
						if (!(nextElement instanceof Inet6Address)) {
							ifs.add(nextElement);
						}
					}
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static synchronized NetIFRoundRobin getInstance() {
		if (instance == null) {
			instance = new NetIFRoundRobin();
		}
		
		return instance;
	}

	public synchronized InetAddress nextIF() {
		return ifs.get(current++ % ifs.size());
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println(NetIFRoundRobin.getInstance().ifs);
		
		BasicHttpParams params = new BasicHttpParams();
		HttpProtocolParams.setUserAgent(params, "Social Networks research crawler, author: Flavio Figueiredo - hp: http://www.dcc.ufmg.br/~flaviov - email: flaviov@dcc.ufmg.br - resume at: http://flaviovdf.googlepages.com/flaviov.d.defigueiredo-resume");
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		
		params.setParameter("http.Accept-Language", "en-us");
		
		//Totals
		ConnManagerParams.setTimeout(params, 10 * 1000 * 60);
		ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(100));
		ConnManagerParams.setMaxTotalConnections(params, 100);
		
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        
        HttpClient httpClient = new DefaultHttpClient(cm, params);
        
        
		for (InetAddress ia : NetIFRoundRobin.getInstance().ifs) {
			System.out.println(ia);
			boolean ok = true;
			try {
				ConnRouteParams.setLocalAddress(params, ia);
				HttpResponse execute = httpClient.execute(new HttpGet("http://www.youtube.com"));
				InputStream content = execute.getEntity().getContent();
				content.close();
			} catch (Exception e) {
				ok = false;
			}
			
			System.out.println(ok);
			System.out.println("--------------");
			System.out.println("--------------");
			System.out.println("--------------");
		}
	}
}
