package br.ufmg.dcc.vod.ncrawler.jobs;

public interface Requester<T> {

	T performRequest(String crawlID) throws Exception;

}
