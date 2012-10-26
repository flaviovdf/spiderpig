package br.ufmg.dcc.vod.ncrawler.filesaver;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.distributed.filesaver.UploadListener;

/**
 * Implementation for the file saver.
 *  
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com 
 */
public class FileSaverImpl implements FileSaver {

	private static final Logger LOG = Logger.getLogger(UploadListener.class);
	private final String saveFolder;
	private final AtomicInteger saved;
	
	/**
	 * Creates a new file saver which will store files at the given folder.
	 * 
	 * @param saveFolder Folder to store files
	 */
	public FileSaverImpl(String saveFolder){
		this.saveFolder = saveFolder;
		this.saved = new AtomicInteger(0);
	}
	
	@Override
	public void save(String fileID, byte[] payload) {
		FileChannel fileChannel = null;
		try {
			File fpath = new File(this.saveFolder, fileID);
			fpath.createNewFile();
			
			fileChannel = 
					FileChannel.open(fpath.toPath(), StandardOpenOption.WRITE);
	
			ByteBuffer buffer = ByteBuffer.wrap(payload);
			buffer.rewind();
			fileChannel.write(buffer);
			
		} catch (IOException e) {
			LOG.error("Unable to save file " + fileID, e);
		} finally {
			if (fileChannel != null) {
				try {
					fileChannel.close();
				} catch (IOException e) {
				}
			}
		}
		
		saved.incrementAndGet();
	}

	@Override
	public int numSaved() {
		return saved.get();
	}
}
