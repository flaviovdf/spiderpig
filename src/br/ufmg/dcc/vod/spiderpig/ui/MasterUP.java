package br.ufmg.dcc.vod.spiderpig.ui;

import java.util.List;

import org.apache.commons.configuration.Configuration;

import br.ufmg.dcc.vod.spiderpig.Crawler;
import br.ufmg.dcc.vod.spiderpig.master.ui.MasterArguments;
import br.ufmg.dcc.vod.spiderpig.master.ui.MasterFactory;

public class MasterUP extends Command {

	@Override
	public void exec(Configuration configuration) throws Exception {
		MasterArguments args = new MasterFactory().configurate(configuration);
		List<String> seed = args.getSeed();
		Crawler crawler = args.getCrawler();
		crawler.dispatch(seed);
		crawler.crawl();
	}

}
