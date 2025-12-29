package org.springframework.data.remote.transport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the response from a transport operation.
 * Contains the result data along with metadata about the response.
 *
 * @param <T> the type of the response body
 */
public class TransportResponse<T> {

    private final T body;
    private final boolean success;
    private final int statusCode;
    private final String errorMessage;
    private final Map<String, String> metadata;

    private TransportResponse(Builder<T> builder) {
        this.body = builder.body;
        this.success = builder.success;
        this.statusCode = builder.statusCode;
        this.errorMessage = builder.errorMessage;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
    }

    /**
     * Creates a successful response with the given body.
     *
     * @param body the response body
     * @param <T>  the body type
     * @return a successful response
     */
    public static <T> TransportResponse<T> success(T body) {
        return TransportResponse.<T>builder()
                .body(body)
                .success(true)
                .statusCode(200)
                .build();
    }

    /**
     * Creates an empty successful response (e.g., for delete operations).
     *
     * @param <T> the expected body type
     * @return a successful response with null body
     */
    public static <T> TransportResponse<T> empty() {
        return TransportResponse.<T>builder()
                .success(true)
                .statusCode(204)
                .build();
    }

    /**
     * Creates a failure response.
     *
     * @param statusCode   the error status code
     * @param errorMessage the error message
     * @param <T>          the expected body type
     * @return a failure response
     */
    public static <T> TransportResponse<T> failure(int statusCode, String errorMessage) {
        return TransportResponse.<T>builder()
                .success(false)
                .statusCode(statusCode)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Creates a builder for TransportResponse.
     *
     * @param <T> the body type
     * @return a new builder
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Returns the response body.
     *
     * @return optional containing the body, or empty if no body
     */
    public Optional<T> getBody() {
        return Optional.ofNullable(body);
    }

    /**
     * Returns whether the operation was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the status code from the transport layer.
     * For REST, this would be the HTTP status code.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the error message if the operation failed.
     *
     * @return optional containing the error message, or empty if successful
     */
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    /**
     * Returns additional metadata from the response.
     * This can include headers, pagination info, etc.
     *
     * @return unmodifiable map of metadata
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "TransportResponse{" +
                "success=" + success +
                ", statusCode=" + statusCode +
                ", hasBody=" + (body != null) +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

    /**
     * Builder for TransportResponse.
     *
     * @param <T> the body type
     */
    public static class Builder<T> {
        private T body;
        private boolean success = true;
        private int statusCode = 200;
        private String errorMessage;
        private Map<String, String> metadata = new HashMap<>();

        private Builder() {
        }

        public Builder<T> body(T body) {
            this.body = body;
            return this;
        }

        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder<T> statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder<T> errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder<T> metadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        public Builder<T> metadata(Map<String, String> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }

        public TransportResponse<T> build() {
            return new TransportResponse<>(this);
        }
    }
}
