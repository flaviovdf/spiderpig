package br.ufmg.dcc.vod.spiderpig.filesaver;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;

/**
 * Saves results in a Level database. Light weight and fast. Also does not
 * save a bazzilion files. One single file for the whole thing 
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class LevelDBSaver implements FileSaver {

    private static final Logger LOG = Logger.getLogger(LevelDBSaver.class);
    private final boolean appendDate;
    private final AtomicInteger saved;
    private final DB db;

    /**
     * Creates a new file saver which will store crawled data at the given 
     * file database.
     * 
     * @param fileName File to store crawled data
     * @param appendDate Appends date to beginning of each key
     */
    public LevelDBSaver(String fileName, boolean appendDate) 
            throws IOException {
        this.appendDate = appendDate;
        this.saved = new AtomicInteger(0);
        Options options = new Options();
        options.createIfMissing(true);
        DBFactory factory = new JniDBFactory();
        this.db = factory.open(new File(fileName), options);
    }
    
    @Override
    public void save(String fileID, byte[] payload) {
        LOG.info("Received file " + fileID + " " + payload.length + " bytes ");
        String fileName = null;
        
        if (appendDate)
            fileName = new Date().getTime() + "-" + fileID;
        else
            fileName = fileID;
        
        this.db.put(fileName.getBytes(), payload);
        this.saved.incrementAndGet();
    }

    @Override
    public int numSaved() {
        return this.saved.get();
    }

    @Override
    public boolean close() throws IOException {
        this.db.close();
        return true;
    }

}
