package br.ufmg.dcc.vod.ncrawler.distributed.nio.common;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import br.ufmg.dcc.vod.ncrawler.common.Tuple;

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
	 * Converts the {@link MessageLite} to a ByteBuffer with the size
	 * of the buffer appended at its beginning (first four bytes).
	 * 
	 * @param msg Message to convert
	 * @return A {@link ByteBuffer} containing an integer (message size) 
	 * 			and the message itself
	 */
	public static ByteBuffer msgToSizedByteBuffer(MessageLite msg) {
		
		byte[] msgAsBytes = msg.toByteArray();
		byte[] result = new byte[msgAsBytes.length + INT_SIZE_BYTES];
		
		System.arraycopy(msgAsBytes, 0, result, INT_SIZE_BYTES, 
				msgAsBytes.length);
		
		ByteBuffer buffer = ByteBuffer.wrap(result);
		buffer.putInt(msgAsBytes.length);
		buffer.rewind();
		
		return buffer;
	}
	
	/**
	 * Reads a {@link MessageLite} from a {@link AsynchronousSocketChannel}
	 * The size of the message must(!) preceed the actual message bytes in 
	 * the buffer.
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
	 * The size of the message must(!) preceed the actual message bytes in 
	 * the buffer.
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
	public static <T extends MessageLite> T readFromBuffer(ByteBuffer buffer,
			Builder builder, ExtensionRegistryLite extensionRegistry) 
					throws InvalidProtocolBufferException {
		Builder merged = 
				builder.mergeFrom(buffer.array(), extensionRegistry);
		MessageLite build = merged.build();
		return (T) build;
	}
}