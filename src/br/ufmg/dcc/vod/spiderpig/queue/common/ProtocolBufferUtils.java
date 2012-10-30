package br.ufmg.dcc.vod.spiderpig.queue.common;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import br.ufmg.dcc.vod.spiderpig.common.Tuple;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLite.Builder;

/**
 * Protocol Buffers serialization and de-serialization utility methods.
 *  
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class ProtocolBufferUtils {

	private static final int INT_SIZE_BYTES = Integer.SIZE / 8;

	/**
	 * Converts the {@link MessageLite} to a ByteBuffer. The buffer will have
	 * the following format:
	 * - (int - 4bytes) message size (without these first 4bytes)
	 * - (int - 4bytes) handle size
	 * - (string - handle size bytes) handle
	 * - (int - 4bytes) byte buffer size
	 * - (byte[] - byte buffer size) byte buffer
	 * 
	 * @param handle Label of object which will receive the message
	 * @param msg Message to convert
	 * @return A {@link ByteBuffer} containing an integer (handle size), 
	 * 			a handle (string) another integer (msg size) and the message 
	 * 			itself.
	 */
	public static ByteBuffer msgToSizedByteBuffer(String toHandle, 
			MessageLite msg) {
		
		byte[] msgAsBytes = msg.toByteArray();
		byte[] handleBytes = toHandle.getBytes();
		int fullMsgSize = handleBytes.length + msgAsBytes.length + 
                			3 * INT_SIZE_BYTES;
		
		ByteBuffer buffer = ByteBuffer.allocate(fullMsgSize);
		buffer.putInt(handleBytes.length + msgAsBytes.length 
				+ 2 * INT_SIZE_BYTES);
		buffer.putInt(handleBytes.length);
		buffer.put(handleBytes);
		buffer.putInt(msgAsBytes.length);
		buffer.put(msgAsBytes);
		buffer.rewind();
		
		return buffer;
	}
	
	/**
	 * Reads a {@link MessageLite} from a {@link AsynchronousSocketChannel}
	 * The object handle which will receive the message, plus the message size
	 * must preceed the message in the handle.
	 * 
	 * @param asch The channel to read the message from
	 * @param builder The builder to create the message from raw bytes
	 * @param extensionRegistry A registry for any extensions the message
	 * 							may contain
	 * 
	 * @return A tuple with a future to wait for the read and the buffer which
	 * will contain the message (without the header).
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws InvalidProtocolBufferException
	 */
	public static Tuple<Future<Integer>, ByteBuffer> 
			readFromChannel(AsynchronousSocketChannel asch) 
					throws InterruptedException, ExecutionException, 
							InvalidProtocolBufferException {

		ByteBuffer intBuffer = ByteBuffer.allocate(INT_SIZE_BYTES);
		asch.read(intBuffer).get();

		intBuffer.rewind();
		int msgSize = intBuffer.getInt();
		
		ByteBuffer messageBuffer = ByteBuffer.allocate(msgSize);
		Future<Integer> read = asch.read(messageBuffer);
		
		return new Tuple<>(read, messageBuffer);
	}

	/**
	 * Reads a {@link MessageLite} from a {@link AsynchronousSocketChannel}
	 * The object handle which will receive the message, plus the message size
	 * must preceed the message in the handle.
	 * 
	 * @param asch The channel to read the message from
	 * @param builder The builder to create the message from raw bytes
	 * @param extensionRegistry A registry for any extensions the message
	 * 							may contain
	 * 
	 * @return A tuple with a future to wait for the read and the buffer which
	 * will contain the message (without the int header).
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws InvalidProtocolBufferException
	 */
	public static void readFromChannel(AsynchronousSocketChannel asch,
			CompletionHandler<Integer, ByteBuffer> handler) {

		ByteBuffer intBuffer = ByteBuffer.allocate(INT_SIZE_BYTES);
		try {
			asch.read(intBuffer).get();
			intBuffer.rewind();
			ByteBuffer messageBuffer = ByteBuffer.allocate(intBuffer.getInt());
			asch.read(messageBuffer, messageBuffer, handler);
		} catch (InterruptedException | ExecutionException e) {
			handler.failed(e, intBuffer);
		}
	}
	
	/**
	 * Reads a string corresponding to the object handle from this buffer
	 * 
	 * @param buffer Buffer to read from
	 * 
	 * @return object handle
	 */
	public static String readHandleFromBuffer(ByteBuffer buffer) {
		int handleSize = buffer.getInt();
		byte[] strBytes = new byte[handleSize];
		buffer.get(strBytes);
		return new String(strBytes);
	}
	
	/**
	 * Converts the {@link ByteBuffer} to a protocol buffer using the given
	 * builder and registry.
	 * 
	 * @param buffer Buffer to read bytes from
	 * @param builder The message builder
	 * @param extensionRegistry The extension registry
	 * 
	 * @return A new {@link MessageLite} object.
	 * 
	 * @throws InvalidProtocolBufferException
	 */
	public static <T extends MessageLite> T 
			readFromBuffer(ByteBuffer buffer, Builder builder, 
					ExtensionRegistryLite extensionRegistry) 
							throws InvalidProtocolBufferException {
		
		buffer.getInt();
		Builder merged = 
				builder.mergeFrom(ByteString.copyFrom(buffer), 
						extensionRegistry);
		MessageLite build = merged.build();
		
		return (T) build;
	}
}