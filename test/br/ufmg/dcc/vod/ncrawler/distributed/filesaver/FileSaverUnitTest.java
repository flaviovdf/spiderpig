package br.ufmg.dcc.vod.ncrawler.distributed.filesaver;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

import junit.framework.Assert;

import org.junit.Test;

import br.ufmg.dcc.vod.ncrawler.common.FileUtil;
import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.MessageListener;
import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.NIOMessageSender;
import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.NIOServer;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaver;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileSaverImpl;
import br.ufmg.dcc.vod.ncrawler.filesaver.FileWrapper;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Payload.UploadMessage;

import com.google.common.io.Files;

public class FileSaverUnitTest {

	private class UploadListenerX extends UploadListener {

		private final SynchronousQueue<Object> queue;

		public UploadListenerX(FileSaver saver, 
				SynchronousQueue<Object> queue) {
			super(saver);
			this.queue = queue;
		}

		@Override
		public void receiveMessage(UploadMessage msg) {
			super.receiveMessage(msg);
			try {
				queue.put(new Object());
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}
		}
		
	}
	
	@Test
	public void testAll() throws Exception {
		File tmp = Files.createTempDir();
		FileSaverImpl impl = new FileSaverImpl(tmp.getAbsolutePath());
		
		SynchronousQueue<Object> waitQueue = new SynchronousQueue<>();
		
		MessageListener<UploadMessage> listener = new UploadListenerX(impl,
				waitQueue);
		NIOServer<UploadMessage> server = new NIOServer<>(2, "localhost", 7676, 
				listener);
		server.start(false);
		
		FileWrapper w1 = new FileWrapper("bah", "oi\nquer\ntc1".getBytes());
		FileWrapper w2 = new FileWrapper("buh", "oi\nquer\ntc2".getBytes());
		
		NIOMessageSender sender = new NIOMessageSender();
		sender.send("localhost", 7676, w1.toProtocolBuffer());
		sender.send("localhost", 7676, w2.toProtocolBuffer());
		
		waitQueue.take();
		waitQueue.take();
		
		List<String> fileAsList = FileUtil.readFileToList(new File(tmp, "bah"));

		Assert.assertEquals(3, fileAsList.size());
		Assert.assertEquals(Arrays.asList("oi", "quer", "tc1"), fileAsList);

		fileAsList = FileUtil.readFileToList(new File(tmp, "buh"));

		Assert.assertEquals(3, fileAsList.size());
		Assert.assertEquals(Arrays.asList("oi", "quer", "tc2"), fileAsList);

		
		server.shutdown();
	}
}
