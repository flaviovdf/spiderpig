package br.ufmg.dcc.vod.spiderpig.ui;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.Crawler;
import br.ufmg.dcc.vod.spiderpig.master.ui.MasterFactory;

public class MasterUP extends Command {

	@Override
	public void exec(Configuration configuration) throws Exception {
		Crawler crawler = 
				new MasterFactory().configurate(configuration);
		System.out.println("Starting Crawler");
		crawler.crawl();
		System.out.println("Crawl Done!!");
	}

}
