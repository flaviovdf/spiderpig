package br.ufmg.dcc.vod.spiderpig.common.queue;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.spiderpig.common.queue.common.ProtocolBufferUtils;
import br.ufmg.dcc.vod.spiderpig.common.queue.serializer.MessageLiteSerializer;

import com.google.protobuf.MessageLite;

/**
 * A multi-threaded socket server where where in each connection a protocol
 * buffer message will be read. 
 *  
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class SocketServer {

    private static final Logger LOG = Logger.getLogger(SocketServer.class);
    
    private final ExecutorService executor;
    private final String hostname;
    private final int port;
    private final QueueService queueService;
    
    private ServerSocket serverSocket;

    public SocketServer(ExecutorService executor, QueueService queueService, 
            String hostname, int port) throws IOException {
        this.executor = executor;
        this.hostname = hostname;
        this.port = port;
        this.queueService = queueService;
        this.serverSocket = null;
    }
    
    /**
     * Starts this service
     * @return true is service was started, false otherwise.
     * 
     * @throws IOException 
     */
    public void start() throws IOException {
        InetSocketAddress addr = 
                new InetSocketAddress(hostname, port);
        this.serverSocket = new ServerSocket();
        this.serverSocket.bind(addr);
        LOG.info("Accepting connections at " + addr + " " + hostname); 
        acceptNextConnection();
    }

    
    private void acceptNextConnection() {
        this.executor.execute(new ServerSocketRunnable());
    }
    
    /**
     * Stops the service.
     * @return true is service was stopped, false otherwise.
     * @throws IOException 
     */
    public void shutdown() throws IOException {
        this.serverSocket.close();
    }
    
    private class ServerSocketRunnable implements Runnable {

        @Override
        public void run() {
            Socket accept = null;
            InputStream inputStream = null;
            try {
                accept = SocketServer.this.serverSocket.accept();
                SocketServer.this.acceptNextConnection();
                
                inputStream = accept.getInputStream();
                String handle = 
                        ProtocolBufferUtils.readHandleFromStream(inputStream);
                
                MessageLiteSerializer<?> serializer = 
                        SocketServer.this.queueService.getSerializer(handle);
                MessageLite msg = 
                        ProtocolBufferUtils.readFromStream(inputStream,
                                    serializer.getBuilder(), 
                                    serializer.getRegistry());
                SocketServer.this.queueService.sendObjectToQueue(handle, msg);
            } catch (IOException | InterruptedException e) {
                LOG.error("Error at connection", e);
            } finally {
                try {
                    if (accept != null)
                        accept.close();
                    
                    if (inputStream != null)
                        inputStream.close();
                    
                } catch (IOException e) {
                    LOG.error("Unable to close", e);
                }
            }
        }
    }
}