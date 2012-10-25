package br.ufmg.dcc.vod.ncrawler.queue;

public class StringSerializer implements Serializer<String> {

	@Override
	public byte[] checkpointData(String t) {
		return t.getBytes();
	}

	@Override
	public String interpret(byte[] checkpoint) {
		return new String(checkpoint);
	}

}
