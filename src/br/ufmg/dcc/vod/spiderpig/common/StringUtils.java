package br.ufmg.dcc.vod.spiderpig.common;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class StringUtils {

	public static Iterable<CrawlID> toCrawlIdIterable(Iterable<String> seeds) {
		return Iterables.transform(seeds, new StringToCrawlID());
	}

	private static class StringToCrawlID implements Function<String, CrawlID> {

		private CrawlID.Builder builder;

		public StringToCrawlID() {
			this.builder = CrawlID.newBuilder();
		}
		
		@Override
		public CrawlID apply(String seedId) {
			return this.builder.setId(seedId).build();
		}
		
	}
}
