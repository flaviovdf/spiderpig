package br.ufmg.dcc.vod.spiderpig.distributed.nio.common;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.junit.Test;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Payload.UploadMessage;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Payload.UploadMessage.Builder;
import br.ufmg.dcc.vod.spiderpig.queue.common.ProtocolBufferUtils;

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
		
		PipedInputStream snk = new PipedInputStream();
		PipedOutputStream piped = new PipedOutputStream(snk);
		
		ProtocolBufferUtils.msgToStream(handle, msg, piped);
		
		String newHandle = ProtocolBufferUtils.readHandleFromStream(snk);
		Assert.assertTrue(newHandle != handle);
		Assert.assertEquals(newHandle, handle);
		
		piped.close();
		UploadMessage newMsg =
				ProtocolBufferUtils.readFromStream(snk, 
						UploadMessage.newBuilder(), null);
		
		Assert.assertTrue(newMsg != msg);
		Assert.assertEquals(newMsg, msg);
		
		snk.close();
	}

}
