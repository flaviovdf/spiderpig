package br.ufmg.dcc.vod.spiderpig.common.queue;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import br.ufmg.dcc.vod.spiderpig.common.queue.common.ProtocolBufferUtils;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.Payload;
import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.Payload.Builder;
import junit.framework.Assert;

public class ProtocolBufferUtilsTest {

	/**
	 * Tests read and write operations
	 */
	@Test
	public void testAll() throws InvalidProtocolBufferException, 
			InterruptedException, ExecutionException, IOException {
		Builder newBuilder = Payload.newBuilder()
				.setPayloadFileName("bah")
				.setPayloadFile(ByteString.copyFrom(new byte[]{1, 2, 3}));
		
		String handle = "JackTheKiller";
		Payload msg = newBuilder.build();
		
		PipedInputStream snk = new PipedInputStream();
		PipedOutputStream piped = new PipedOutputStream(snk);
		
		ProtocolBufferUtils.msgToStream(handle, msg, piped);
		
		String newHandle = ProtocolBufferUtils.readHandleFromStream(snk);
		Assert.assertTrue(newHandle != handle);
		Assert.assertEquals(newHandle, handle);
		
		piped.close();
		Payload newMsg =
				ProtocolBufferUtils.readFromStream(snk, 
						Payload.newBuilder(), null);
		
		Assert.assertTrue(newMsg != msg);
		Assert.assertEquals(newMsg, msg);
		
		snk.close();
	}

}
