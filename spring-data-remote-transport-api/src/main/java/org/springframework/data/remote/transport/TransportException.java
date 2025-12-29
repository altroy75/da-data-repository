package org.springframework.data.remote.transport;

/**
 * Exception thrown when a transport operation fails.
 * This is a runtime exception that wraps transport-specific errors
 * into a common exception type.
 */
public class TransportException extends RuntimeException {

    private final int statusCode;
    private final String resourceName;
    private final TransportOperation operation;

    /**
     * Creates a new transport exception.
     *
     * @param message      the error message
     * @param statusCode   the status code from the transport layer
     * @param resourceName the resource that was being accessed
     * @param operation    the operation that failed
     */
    public TransportException(String message, int statusCode, String resourceName, TransportOperation operation) {
        super(message);
        this.statusCode = statusCode;
        this.resourceName = resourceName;
        this.operation = operation;
    }

    /**
     * Creates a new transport exception with a cause.
     *
     * @param message      the error message
     * @param cause        the underlying cause
     * @param statusCode   the status code from the transport layer
     * @param resourceName the resource that was being accessed
     * @param operation    the operation that failed
     */
    public TransportException(String message, Throwable cause, int statusCode, String resourceName,
            TransportOperation operation) {
        super(message, cause);
        this.statusCode = statusCode;
        this.resourceName = resourceName;
        this.operation = operation;
    }

    /**
     * Creates a transport exception for a connection failure.
     *
     * @param message      the error message
     * @param cause        the underlying cause
     * @param resourceName the resource that was being accessed
     * @return a new transport exception
     */
    public static TransportException connectionFailure(String message, Throwable cause, String resourceName) {
        return new TransportException(message, cause, 0, resourceName, null);
    }

    /**
     * Returns the status code from the transport layer.
     *
     * @return the status code, or 0 if not applicable
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the resource that was being accessed.
     *
     * @return the resource name
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Returns the operation that failed.
     *
     * @return the operation, or null if not applicable
     */
    public TransportOperation getOperation() {
        return operation;
    }

    /**
     * Returns whether this is a client error (4xx status codes in REST).
     *
     * @return true if this is a client error
     */
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * Returns whether this is a server error (5xx status codes in REST).
     *
     * @return true if this is a server error
     */
    public boolean isServerError() {
        return statusCode >= 500;
    }

    /**
     * Returns whether the resource was not found (404 in REST).
     *
     * @return true if the resource was not found
     */
    public boolean isNotFound() {
        return statusCode == 404;
    }

    @Override
    public String toString() {
        return "TransportException{" +
                "message='" + getMessage() + '\'' +
                ", statusCode=" + statusCode +
                ", resourceName='" + resourceName + '\'' +
                ", operation=" + operation +
                '}';
    }
}
