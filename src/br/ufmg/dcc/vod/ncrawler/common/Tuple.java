package br.ufmg.dcc.vod.ncrawler.common;

import java.io.Serializable;

/**
 * Represents a Tuple
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class Tuple<A, B> implements Serializable {
	
	private static final long serialVersionUID = Constants.SERIAL_UID;

	public final A first;
	
	public final B second;
	
	/**
	 * Creates a new Tuple with the given elements
	 * 
	 * @param first The first element of the tuple
	 * @param second The second element of the tuple
	 */
	public Tuple(A first, B second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public String toString() {
		return "{" + first + ": " + second + "}";
	}
}
