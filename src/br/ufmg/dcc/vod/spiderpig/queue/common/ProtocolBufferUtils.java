package br.ufmg.dcc.vod.spiderpig.queue.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.google.protobuf.ExtensionRegistryLite;
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
	 * Sends the {@link MessageLite} to the stream. Before the actual message
	 * an int (4 bytes) and a string handle will be written.
	 * 
	 * - (int - 4bytes) handle size
	 * - (string - handle size bytes) handle
	 * - (byte[] - byte buffer size) byte buffer
	 * 
	 * @param handle Label of object which will receive the message
	 * @param msg Message to convert
	 * @param stream Stream to write to
	 * 
	 * @return A {@link ByteBuffer} containing an integer (handle size), 
	 * 			a handle (string) another integer (msg size) and the message 
	 * 			itself.
	 * @throws IOException 
	 */
	public static void msgToStream(String toHandle, MessageLite msg, 
			OutputStream stream) throws IOException {
		
		ByteBuffer intBuffer = ByteBuffer.allocate(INT_SIZE_BYTES);
		byte[] handleBytes = toHandle.getBytes();
		
		intBuffer.putInt(handleBytes.length);
		intBuffer.rewind();
		stream.write(intBuffer.array());
		stream.write(handleBytes);
		msg.writeTo(stream);
		stream.flush();
	}
	
	/**
	 * Reads a string corresponding to the object handle from this buffer.
	 * The size of the string as a 4 byte int must precede it.
	 * 
	 * @param inputStream Stream to read from
	 * 
	 * @return object handle
	 */
	public static String readHandleFromStream(InputStream inputStream) 
			throws IOException {
		ByteBuffer intBuffer = ByteBuffer.allocate(INT_SIZE_BYTES);
		inputStream.read(intBuffer.array());
		
		intBuffer.rewind();
		ByteBuffer handleBuffer = ByteBuffer.allocate(intBuffer.getInt());
		
		inputStream.read(handleBuffer.array());
		handleBuffer.rewind();
		
		return new String(handleBuffer.array());
	}

	/**
	 * Reads a message from the stream using the given builder and registry.
	 * 
	 * @param inputStream stream to read bytes from
	 * @param builder The message builder
	 * @param extensionRegistry The extension registry
	 * 
	 * @return A new {@link MessageLite} object.
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static <T extends MessageLite> T readFromStream(
			InputStream inputStream, Builder builder, 
					ExtensionRegistryLite registry) 
							throws IOException {
		
		Builder merged = builder.mergeFrom(inputStream, registry);
		MessageLite build = merged.build();
		
		return (T) build;
	}
}