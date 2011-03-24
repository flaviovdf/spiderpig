package br.ufmg.dcc.vod.ncrawler.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.user_apihtml.LastFMArtistDAO;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.user_apihtml.LastFMTagDAO;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube.user_api.YoutubeUserDAO;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube.video_api.YoutubeVideoDAO;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.CGLIBEnhancedConverter;
import com.thoughtworks.xstream.mapper.CGLIBMapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class MyXStreamer {

	private static MyXStreamer instance;
	private XStream stream;
	
	private MyXStreamer() {
		XStream xstream = new XStream() {
			protected MapperWrapper wrapMapper(MapperWrapper next) {
				return new CGLIBMapper(next);
			}
		};
		xstream.registerConverter(new CGLIBEnhancedConverter(xstream.getMapper(), xstream.getReflectionProvider()));
		this.stream = xstream;
		this.stream.alias("lf-tag", LastFMTagDAO.class);
		this.stream.alias("lf-artist", LastFMArtistDAO.class);
		this.stream.alias("br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_videos.YoutubeVideoDAO", 
				YoutubeVideoDAO.class);
		this.stream.alias("br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector.YoutubeUserDAO", 
				YoutubeUserDAO.class);
	}
	
	public static MyXStreamer getInstance() {
		if (instance == null) instance = new MyXStreamer();
		return instance;
	}

	public void toXML(Object o, File file) throws IOException {
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new FileWriter(file));
			this.stream.toXML(o, w);
		} finally {
			if (w != null) w.close();
		}
	}

	public void toXML(Object o, FileDescriptor out) throws IOException {
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new FileWriter(out));
			this.stream.toXML(o, w);
		} finally {
			if (w != null) w.close();
		}
	}
	
	public Object fromXML(File file) throws IOException {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(file));
			return this.stream.fromXML(r);
		} finally {
			if (r != null) r.close();
		}
	}
}