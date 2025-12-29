package org.springframework.data.remote.transport;

/**
 * Marker interface for transport client configuration.
 * Each transport implementation provides its own configuration class
 * implementing this interface.
 *
 * <p>Examples:
 * <ul>
 *     <li>RestTransportConfig - REST-specific settings (baseUrl, headers, timeout)</li>
 *     <li>GrpcTransportConfig - gRPC-specific settings (host, port, credentials)</li>
 * </ul>
 */
public interface TransportClientConfig {

    /**
     * Returns the base URL or connection string for the remote service.
     *
     * @return the base URL/connection string
     */
    String getBaseUrl();
}
