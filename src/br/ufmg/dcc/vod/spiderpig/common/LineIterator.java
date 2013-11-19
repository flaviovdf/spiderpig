package br.ufmg.dcc.vod.spiderpig.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

class LineIterator implements Iterator<String> {

	private final File file;
	private BufferedReader reader;
	private boolean closed;
	private String nextLine;

	public LineIterator(File file) {
		this.file = file;
		this.reader = null;
		this.closed = false;
		this.nextLine = null;
	}

	@Override
	public boolean hasNext() {		
		try {
			if (this.reader == null) {
				this.reader = new BufferedReader(new FileReader(this.file));
			}
			
			this.nextLine = reader.readLine();
			if (this.nextLine == null) {
				close();
			}
		} catch (IOException e) {
			try {
				close();
			} catch (IOException e1) {
			}
		}
		
		return !this.closed;
	}

	private void close() throws IOException {
		if (this.reader != null) {
			this.reader.close();
		}
		this.closed = true;
	}

	@Override
	public String next() throws NoSuchElementException {
		return this.nextLine;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}