package br.ufmg.dcc.vod.ncrawler.stats;

import java.util.Map;

/**
 * A Display is used to update some UI on crawling status.
 * Status are based on {@String}s and {@code Integer}s.
 *  
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public interface Display {

	/**
	 * At every update. Some property define by the <@code String> key
	 * will have a value defined by the {@code Integer} value.
	 * 
	 * @param map The map with update values.
	 */
	public void print(Map<String, Integer> map);
	
}
