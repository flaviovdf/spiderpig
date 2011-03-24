package br.ufmg.dcc.vod.ncrawler.common;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class QueueChecker {

	public static void main(String[] args) throws Exception {
		FileChannel channel = new RandomAccessFile(new File(args[0]), "rw").getChannel();
		MappedByteBuffer map = channel.map(MapMode.READ_WRITE, 0, 12);
		
		System.out.println(map.getInt());
		System.out.println(map.getInt());
		System.out.println(map.getInt());
	}
	
}
