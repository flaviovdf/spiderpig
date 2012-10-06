package br.ufmg.dcc.vod.ncrawler.jobs.generic;

import br.ufmg.dcc.vod.ncrawler.common.SignedUtils;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public abstract class AbstractArraySerializer<T> implements Serializer<T> {

	private final int numArrays;

	public AbstractArraySerializer(int numArrays) {
		this.numArrays = numArrays;
	}

	@Override
	public byte[] checkpointData(T t) {
		byte[][] bs = getArrays(t);
		
		if (bs.length != numArrays) {
			throw new RuntimeException();
		}
		
		int size = numArrays;
		for (int i = 0; i < numArrays; i++) {
			size += bs[i].length;
		}
		
		byte[] res = new byte[size];
		int pos = numArrays;
		for (int i = 0; i < numArrays; i++) {
			res[i] = SignedUtils.signedByte(bs[i].length);
			System.arraycopy(bs[i], 0, res, pos, bs[i].length);
			pos += bs[i].length;
		}
		
		return res;
	}

	public abstract byte[][] getArrays(T t);

	public T interpret(byte[] checkpoint) {
		byte[][] bs = new byte[numArrays][];
		
		for (int i = 0; i < numArrays; i++) {
			bs[i] = new byte[SignedUtils.unsignedByte(checkpoint[i])];
		}
		
		int pos = numArrays;
		for (int i = 0; i < numArrays; i++) {
			System.arraycopy(checkpoint, pos, bs[i], 0, bs[i].length);
			pos += bs[i].length;
		}
		
		return setValueFromArrays(bs);
	}

	public abstract T setValueFromArrays(byte[][] bs);
}
