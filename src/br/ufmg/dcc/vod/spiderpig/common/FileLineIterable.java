package br.ufmg.dcc.vod.spiderpig.common;

import java.io.File;
import java.util.Iterator;

public class FileLineIterable implements Iterable<String> {

    private File file;
    private int bufferSize;

    public FileLineIterable(File file, int bufferSize) {
        this.file = file;
        this.bufferSize = bufferSize;
    }
    
    public FileLineIterable(String file, int bufferSize) {
        this(new File(file), bufferSize);
    }
    
    @Override
    public Iterator<String> iterator() {
        return new LineIterator(this.file, this.bufferSize);
    }
}