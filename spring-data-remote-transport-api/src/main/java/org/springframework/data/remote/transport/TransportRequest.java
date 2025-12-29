package org.springframework.data.remote.transport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a request to be executed by a transport client.
 * This is a protocol-agnostic representation that transport implementations
 * translate into their specific format (HTTP request, gRPC call, etc.).
 *
 * <p>
 * Use the {@link #builder()} method to create instances.
 */
public class TransportRequest {

    private final TransportOperation operation;
    private final String resourceName;
    private final Map<String, Object> parameters;
    private final Object payload;
    private final Object id;
    private final Class<?> entityType;

    private TransportRequest(Builder builder) {
        this.operation = builder.operation;
        this.resourceName = builder.resourceName;
        this.parameters = Collections.unmodifiableMap(new HashMap<>(builder.parameters));
        this.payload = builder.payload;
        this.id = builder.id;
        this.entityType = builder.entityType;
    }

    /**
     * Creates a new builder for constructing transport requests.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the operation to be performed.
     *
     * @return the transport operation
     */
    public TransportOperation getOperation() {
        return operation;
    }

    /**
     * Returns the name of the resource being accessed.
     * This typically maps to an endpoint path segment (e.g., "users", "orders").
     *
     * @return the resource name
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Returns additional parameters for the request.
     * These may be used for query parameters, filtering, pagination, etc.
     *
     * @return unmodifiable map of parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Returns the payload for write operations (SAVE).
     *
     * @return optional containing the payload, or empty for read operations
     */
    public Optional<Object> getPayload() {
        return Optional.ofNullable(payload);
    }

    /**
     * Returns the entity identifier for single-entity operations.
     *
     * @return optional containing the ID, or empty if not applicable
     */
    public Optional<Object> getId() {
        return Optional.ofNullable(id);
    }

    /**
     * Returns the entity type being operated on.
     *
     * @return the entity class
     */
    public Class<?> getEntityType() {
        return entityType;
    }

    @Override
    public String toString() {
        return "TransportRequest{" +
                "operation=" + operation +
                ", resourceName='" + resourceName + '\'' +
                ", id=" + id +
                ", parameters=" + parameters +
                ", hasPayload=" + (payload != null) +
                '}';
    }

    /**
     * Builder for creating TransportRequest instances.
     */
    public static class Builder {
        private TransportOperation operation;
        private String resourceName;
        private Map<String, Object> parameters = new HashMap<>();
        private Object payload;
        private Object id;
        private Class<?> entityType;

        private Builder() {
        }

        public Builder operation(TransportOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder resourceName(String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        public Builder parameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        public Builder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder entityType(Class<?> entityType) {
            this.entityType = entityType;
            return this;
        }

        public TransportRequest build() {
            if (operation == null) {
                throw new IllegalStateException("Operation is required");
            }
            if (resourceName == null || resourceName.isBlank()) {
                throw new IllegalStateException("Resource name is required");
            }
            return new TransportRequest(this);
        }
    }
}
