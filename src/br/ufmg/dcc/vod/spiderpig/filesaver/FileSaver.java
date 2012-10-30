package br.ufmg.dcc.vod.spiderpig.filesaver;

/**
 * Interface for the file saver. Implementations of this class are responsible
 * for saving crawled file to disk.
 *  
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com 
 */
public interface FileSaver {

	/**
	 * Save the file with the given id and content
	 * 
	 * @param fileID ID of the file to save
	 * @param payload Content of the file
	 */
	public void save(String fileID, byte[] payload);

	/**
	 * Get's the number of saved files
	 * 
	 * @return number of saved files
	 */
	public int numSaved();
	
}
