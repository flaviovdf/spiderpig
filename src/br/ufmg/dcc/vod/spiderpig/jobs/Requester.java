package br.ufmg.dcc.vod.spiderpig.jobs;

public interface Requester<T> {

	T performRequest(String crawlID) throws Exception;

}
