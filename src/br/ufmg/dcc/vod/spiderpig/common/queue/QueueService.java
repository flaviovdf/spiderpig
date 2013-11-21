package br.ufmg.dcc.vod.spiderpig.common.queue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import br.ufmg.dcc.vod.spiderpig.common.Tuple;
import br.ufmg.dcc.vod.spiderpig.common.queue.basequeues.MultiFileMMapFifoQueue;
import br.ufmg.dcc.vod.spiderpig.common.queue.basequeues.SimpleEventQueue;
import br.ufmg.dcc.vod.spiderpig.common.queue.serializer.MessageLiteSerializer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.protobuf.MessageLite;

/**
 * QueueServices are used to create MonitoredSyncQueues, add objects to these
 * queues and register threads which will consume objects from queues.
 * 
 * This class is thread safe
 * 
 * @param <T> Type of objects in a queue
 */
public class QueueService {

    private class ServiceStruct<T extends MessageLite> {
        final MonitoredSyncQueue queue;
        final List<WorkerRunnable<?>> runnables;
        final Actor<T> actor;
        
        public ServiceStruct(MonitoredSyncQueue queue, Actor<T> actor) {
            this.queue = queue;
            this.runnables = new ArrayList<>();
            this.actor = actor;
        }
    }
    
    private final Map<String, ServiceStruct<?>> ids = new HashMap<>();
    
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final SocketServer sserver;

    private final String ip;
    private final int port;
    private final long sessionID;

    public QueueService() throws IOException {
        this.ip = InetAddress.getLocalHost().getHostAddress();
        this.port = -1;
        this.sserver = null;
        this.sessionID = Math.round(Math.random() * Long.MAX_VALUE);
    }
    
    public QueueService(int port) throws IOException {
        this(InetAddress.getLocalHost().getHostName(), port);
    }
    
    public QueueService(String hostname, int port) throws IOException {
        Preconditions.checkNotNull(hostname);
        this.ip = InetAddress.getByName(hostname).getHostAddress();
        this.port = port;
        this.sessionID = Math.round(Math.random() * Long.MAX_VALUE);
        this.sserver = new SocketServer(this.executor, this, this.ip, 
                this.port);
        this.sserver.start();
    }
    
    /**
     * Creates a new message queue with a label
     *
     * @param label Label
     */
    <T extends MessageLite> void createMessageQueue(Actor<T> actor) {
        try {
            lock.writeLock().lock();
            if (this.ids.containsKey(actor.getHandle()))
                throw new QueueServiceException("Label already exists in service");
            
            MonitoredSyncQueue queue = 
                    new MonitoredSyncQueue(new SimpleEventQueue<T>());
            ServiceStruct<T> struct = new ServiceStruct<>(queue, actor);
            this.ids.put(actor.getHandle(), struct);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Creates a new message queue that is stored on multiple files on disk
     *
     * @param label Label
     * @param folder Folder to use
     * @param serializer To serialize object
     * @param bytes amount of bytes to allocate on file
     * 
     * @throws IOException In case and io error occurs 
     * @throws FileNotFoundException  In case the file does not exist
     */
    <T extends MessageLite> void createPersistentMessageQueue(
            Actor<T> actor, File folder, int bytes) 
                    throws FileNotFoundException, IOException {
        try {
            lock.writeLock().lock();
            if (this.ids.containsKey(actor.getHandle()))
                throw new QueueServiceException("Label already exists in service");
            
            if (!folder.isDirectory())
                throw new IOException("Not a folder");
            
            MessageLiteSerializer<T> serializer = actor.newMsgSerializer();
            MultiFileMMapFifoQueue<T> memoryMappedQueue = 
                    new MultiFileMMapFifoQueue<T>(folder, serializer , bytes);
            MonitoredSyncQueue queue = 
                    new MonitoredSyncQueue(memoryMappedQueue);
            ServiceStruct<T> struct = new ServiceStruct<>(queue, actor);
            this.ids.put(actor.getHandle(), struct);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Starts a QueueProcessor on a new Thread. It will consume the queue with
     * the given handle.
     * 
     * @param l String identifying the queue
     * @param p QueueProcessor object which will process
     */
    <T extends MessageLite> void startProcessor(String l, 
            QueueProcessor<T> p) {
        try {
            lock.writeLock().lock();
            if (!this.ids.containsKey(l)) {
                throw new QueueServiceException("Unknown handle");
            }
            
            ServiceStruct<?> serviceStruct = this.ids.get(l);
            MonitoredSyncQueue queue = serviceStruct.queue;
            List<WorkerRunnable<?>> runnables = serviceStruct.runnables;
            WorkerRunnable<T> runnable = new WorkerRunnable<T>(queue, p);
            runnables.add(runnable);
            executor.execute(runnable);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Insert an object to a given queue so it can be processed by a 
     * QueueProcessor.
     * 
     * @param l The label of the queue
     * @param t The object to insert
     * 
     * @throws InterruptedException 
     */
    <T extends MessageLite> void sendObjectToQueue(String l, T t) 
            throws InterruptedException {
        MonitoredSyncQueue monitoredSyncQueue;
        try {
            lock.readLock().lock();
            if (!this.ids.containsKey(l)) {
                throw new QueueServiceException("Unknown handle");
            }
            monitoredSyncQueue = this.ids.get(l).queue;
        } finally {
            lock.readLock().unlock();
        }
        monitoredSyncQueue.put(t);
    }
    
    /**
     * Waits until multiple queues are empty. The algorithm does two passes on the
     * queues, one to see if they are empty and the other to assure that the work
     * that was being done by the queues did not add any objects on the other queues. 
     * <br>
     * When using queues for communication it is important to only add an element to a
     * communication queue after calling the <code>done</code> method on the queue the
     * current thread is consuming. This is done by the <code>WorkerRunnable</code> class. 
     * 
     * @param secondsBetweenChecks - Seconds between verifications
     */
    public void waitUntilWorkIsDone(int secondsBetweenChecks) {
        boolean someoneIsWorking = false;
        do {
            try {
                lock.readLock().lock();
                someoneIsWorking = false;
                
                //System.err.println("-- debug " + new Date());
                //for (MonitoredSyncQueue<?> m : ids.values()) {
                //  System.err.println(m + " => " + m.size());
                //}
                
                //Acquiring time stamps
                int[] stamps = new int[ids.size()];
                int i = 0;
                for (ServiceStruct<?> struct : ids.values()) {
                    MonitoredSyncQueue queue = struct.queue;
                    Tuple<Integer, Integer> sizeAndTimeStamp = 
                            queue.synchronizationData();
                    if (sizeAndTimeStamp.first != 0) {
                        someoneIsWorking = true;
                        break;
                    } else {
                        stamps[i] = sizeAndTimeStamp.second;
                    }
                    i++;
                }
    
                //Verifying if stamps changed
                i = 0;
                if (!someoneIsWorking) {
                    for (ServiceStruct<?> struct : ids.values()) {
                        MonitoredSyncQueue queue = struct.queue;
                        Tuple<Integer, Integer> sizeAndTimeStamp = 
                                queue.synchronizationData();
                        if (sizeAndTimeStamp.first != 0 || stamps[i] != sizeAndTimeStamp.second) {
                            someoneIsWorking = true;
                            break;
                        }
                        i++;
                    }
                }
            } finally {
                lock.readLock().unlock();
            }
            
            if (someoneIsWorking) {
                try {
                    Thread.sleep(secondsBetweenChecks * 1000);
                } catch (InterruptedException e) {
                }
            }
        } while (someoneIsWorking);
    }
    
    public void waitUntilWorkIsDoneAndStop(int secondsBetweenChecks) {
        waitUntilWorkIsDone(secondsBetweenChecks);
        stop();
    }

    private void stop() {
        try {
            for (ServiceStruct<?> struct : ids.values()) {
                MonitoredSyncQueue m = struct.queue;
                m.poison();
                
                for (WorkerRunnable<?> runnable : struct.runnables)
                    runnable.awaitTermination();
            }
            
            if (this.sserver != null)
                this.sserver.shutdown();
            this.executor.shutdown();
            this.executor.awaitTermination(Long.MAX_VALUE, 
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException | IOException e) {
            throw new QueueServiceException(e);
        }
    }
    
    /**
     * A worker runnable guarantees that the done method of the queue is called. 
     */
    private class WorkerRunnable<T extends MessageLite> implements Runnable {
        
        private final MonitoredSyncQueue q;
        private final QueueProcessor<T> p;
        private final CountDownLatch latch;
        
        public WorkerRunnable(MonitoredSyncQueue q, QueueProcessor<T> p) {
            this.q = q;
            this.p = p;
            this.latch = new CountDownLatch(1);
        }
        

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            boolean interrupted = false;
            while (!interrupted) {
                MessageLite take = q.take();
                
                if (take != MonitoredSyncQueue.POISON) {
                    p.process((T) take);
                    q.done(take);
                } else {
                    interrupted = true;
                }
            }
            latch.countDown();
        }
        
        public void awaitTermination() throws InterruptedException {
            latch.await();
        }
    }
    
    MessageLiteSerializer<?> getSerializer(String handle) throws IOException {
        try {
            this.lock.readLock().lock();
            ServiceStruct<?> serviceStruct = this.ids.get(handle);
            
            if (serviceStruct == null) {
                throw new IOException("Unknown handle " + handle);
            }
            
            return serviceStruct.actor.newMsgSerializer();
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    @VisibleForTesting MonitoredSyncQueue getMessageQueue(String label) {
        return this.ids.get(label).queue;
    }

    public String getIP() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public long getSessionID() {
        return sessionID;
    }
}
