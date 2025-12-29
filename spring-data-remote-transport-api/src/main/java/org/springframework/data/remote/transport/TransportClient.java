package org.springframework.data.remote.transport;

import java.util.List;

/**
 * Core interface for transport layer implementations.
 * This is the main SPI (Service Provider Interface) that must be implemented
 * by each communication protocol (REST, gRPC, GraphQL, etc.).
 *
 * <p>
 * Transport clients are responsible for:
 * <ul>
 * <li>Translating {@link TransportRequest} into protocol-specific calls</li>
 * <li>Executing the remote call</li>
 * <li>Translating the response into {@link TransportResponse}</li>
 * <li>Handling protocol-specific errors</li>
 * </ul>
 *
 * <p>
 * Implementations should be thread-safe as they may be shared across
 * multiple repository instances.
 *
 * @param <C> the configuration type for this transport
 */
public interface TransportClient<C extends TransportClientConfig> {

    /**
     * Executes a transport request and returns a single entity response.
     *
     * @param request      the transport request to execute
     * @param responseType the expected response type
     * @param <T>          the response entity type
     * @return the transport response containing the entity
     * @throws TransportException if the transport operation fails
     */
    <T> TransportResponse<T> execute(TransportRequest request, Class<T> responseType);

    /**
     * Executes a transport request and returns a list of entities.
     *
     * @param request     the transport request to execute
     * @param elementType the type of elements in the list
     * @param <T>         the element type
     * @return the transport response containing a list of entities
     * @throws TransportException if the transport operation fails
     */
    <T> TransportResponse<List<T>> executeForList(TransportRequest request, Class<T> elementType);

    /**
     * Configures the transport client with the provided configuration.
     * This is called during initialization.
     *
     * @param config the transport-specific configuration
     */
    void configure(C config);

    /**
     * Returns the configuration class type for this transport.
     * Used for type-safe configuration binding.
     *
     * @return the configuration class
     */
    Class<C> getConfigType();
}
