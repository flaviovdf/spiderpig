package br.ufmg.dcc.vod.ncrawler.distributed.nio.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.Test;

import br.ufmg.dcc.vod.ncrawler.common.Tuple;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Payload.UploadMessage;
import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Payload.UploadMessage.Builder;
import br.ufmg.dcc.vod.ncrawler.queue.common.ProtocolBufferUtils;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class ProtocolBufferUtilsTest {

	/**
	 * Tests read and write operations
	 */
	@Test
	public void testAll() throws InvalidProtocolBufferException, 
			InterruptedException, ExecutionException, IOException {
		Builder newBuilder = UploadMessage.newBuilder()
				.setFileName("bah")
				.setPayload(ByteString.copyFrom(new byte[]{1, 2, 3}));
		
		String handle = "JackTheKiller";
		UploadMessage msg = newBuilder.build();
		ByteBuffer buff = 
				ProtocolBufferUtils.msgToSizedByteBuffer(handle, msg);
		
		byte[] protoArray = msg.toByteArray();
		byte[] handleArray = handle.getBytes();
		
		int msgSize = handleArray.length + protoArray.length;
		Assert.assertEquals(msgSize + 12, buff.capacity());
		
		Assert.assertEquals(msgSize + 8, buff.getInt());
		Assert.assertEquals(handleArray.length, buff.getInt());
		for (int i = 0; i < handleArray.length; i++) {
			Assert.assertEquals(handleArray[i], buff.get());
		}
	
		Assert.assertEquals(protoArray.length, buff.getInt());
		for (int i = 0; i < protoArray.length; i++)
			Assert.assertEquals(protoArray[i], buff.get());
		buff.rewind();
		
		InetSocketAddress addr = new InetSocketAddress(2222);
		AsynchronousServerSocketChannel server = 
				AsynchronousServerSocketChannel.open().bind(addr);
		AsynchronousSocketChannel asch = AsynchronousSocketChannel.open();
		asch.connect(addr).get();
		
		server.accept().get().write(buff).get();
		
		Tuple<Future<Integer>, ByteBuffer> tuple = 
				ProtocolBufferUtils.readFromChannel(asch);
		
		Assert.assertEquals(msgSize + 8, tuple.first.get().intValue());
		ByteBuffer newBuff = tuple.second;
		newBuff.rewind();
		
		String newHandle = ProtocolBufferUtils.readHandleFromBuffer(newBuff);
		Assert.assertTrue(newHandle != handle);
		Assert.assertEquals(newHandle, handle);
		
		UploadMessage newMsg =
				ProtocolBufferUtils.readFromBuffer(newBuff, 
						UploadMessage.newBuilder(), null);
		
		Assert.assertTrue(newMsg != msg);
		Assert.assertEquals(newMsg, msg);
		
		server.close();
		asch.close();
	}

}
