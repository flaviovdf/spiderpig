package br.ufmg.dcc.vod.spiderpig.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;

/**
 * Utility method for creating {@link ServiceID}s with resolved host, that is,
 * ip addresses. This is useful for securing that ids to the same host will
 * always be equal. This is the preferred way to build ids.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class ServiceIDUtils {

    /**
     * Creates a new {@link ServiceID} resolving the hostname to an ip address.
     * 
     * @param hostname Hostname to resolve
     * @param port Port of the service
     * @param handle Service handle
     * 
     * @return The built service id.
     * 
     * @throws UnknownHostException If hostname cannot be resolved.
     */
    public static ServiceID toResolvedServiceID(String hostname, int port, 
            String handle) throws UnknownHostException {
        String hostIP = InetAddress.getByName(hostname).getHostAddress();
        ServiceID.Builder builder = ServiceID.newBuilder();
        return builder
                .setHostname(hostIP).setPort(port).setHandle(handle).build();
    }
    
}