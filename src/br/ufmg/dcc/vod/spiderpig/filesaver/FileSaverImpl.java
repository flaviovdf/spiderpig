package br.ufmg.dcc.vod.spiderpig.filesaver;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * Implementation for the file saver.
 *  
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com 
 */
public class FileSaverImpl implements FileSaver {

	private static final Logger LOG = Logger.getLogger(FileSaverImpl.class);
	private final String saveFolder;
	private final AtomicInteger saved;
	private final boolean appendDate;
	
	/**
	 * Creates a new file saver which will store files at the given folder.
	 * 
	 * @param saveFolder Folder to store files
	 * @param appendDate Appends date to beggining of file
	 */
	public FileSaverImpl(String saveFolder, boolean appendDate){
		this.saveFolder = saveFolder;
		this.appendDate = appendDate;
		this.saved = new AtomicInteger(0);
	}
	
	@Override
	public void save(String fileID, byte[] payload) {
		LOG.info("Received file " + fileID + " " + payload.length + " bytes ");
		FileChannel fileChannel = null;
		try {
			String fileName = null;
			
			if (appendDate)
				fileName = new Date().getTime() + "-" + fileID;
			else
				fileName = fileID;
			
			File fpath = new File(this.saveFolder, fileName);
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
					LOG.error("Unable to close channel" + fileID, e);
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
