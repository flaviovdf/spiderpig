package br.ufmg.dcc.vod.spiderpig.common;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class FileLineIterable implements Iterable<String> {

    private File file;

    public FileLineIterable(File file) throws IOException {
        this.file = file;
    }
    
    @Override
    public Iterator<String> iterator() {
        return new LineIterator(file);
    }
}