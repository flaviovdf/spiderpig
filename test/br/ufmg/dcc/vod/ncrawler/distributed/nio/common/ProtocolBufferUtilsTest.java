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
		
		UploadMessage msg = newBuilder.build();
		ByteBuffer buff = 
				ProtocolBufferUtils.msgToSizedByteBuffer(msg);
		buff.rewind();
		
		byte[] byteArray = msg.toByteArray();
		Assert.assertEquals(byteArray.length + 4, buff.capacity());
		Assert.assertEquals(byteArray.length, buff.getInt());
		
		for (int i = 0; i < byteArray.length; i++)
			Assert.assertEquals(byteArray[i], buff.get());
		buff.rewind();
		
		InetSocketAddress addr = new InetSocketAddress(2222);
		AsynchronousServerSocketChannel server = 
				AsynchronousServerSocketChannel.open().bind(addr);
		AsynchronousSocketChannel asch = AsynchronousSocketChannel.open();
		asch.connect(addr).get();
		
		server.accept().get().write(buff).get();
		
		Tuple<Future<Integer>, ByteBuffer> tuple = 
				ProtocolBufferUtils.readFromChannel(asch);
		
		Assert.assertEquals(tuple.first.get().intValue(), byteArray.length);
		ByteBuffer newBuff = tuple.second;
		
		UploadMessage newMsg = ProtocolBufferUtils.readFromBuffer(newBuff, 
				UploadMessage.newBuilder(), null);
		
		Assert.assertTrue(newMsg != msg);
		Assert.assertEquals(newMsg, msg);
		
		server.close();
		asch.close();
	}

}
