package br.ufmg.dcc.vod.ncrawler.common;
import java.io.Serializable;
public class Pair<T1, T2> implements Serializable {

	public final T1 first;
	
	public final T2 second;
	
	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public String toString() {
		return "{" + first + ": " + second + "}";
	}
}
