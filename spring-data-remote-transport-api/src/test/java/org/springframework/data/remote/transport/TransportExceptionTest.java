package org.springframework.data.remote.transport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransportExceptionTest {

    @Test
    void shouldCreateException() {
        TransportException exception = new TransportException(
                "Not found",
                404,
                "users",
                TransportOperation.FIND_BY_ID);

        assertEquals("Not found", exception.getMessage());
        assertEquals(404, exception.getStatusCode());
        assertEquals("users", exception.getResourceName());
        assertEquals(TransportOperation.FIND_BY_ID, exception.getOperation());
    }

    @Test
    void shouldIdentifyClientError() {
        TransportException exception = new TransportException("Bad request", 400, "users", null);
        assertTrue(exception.isClientError());
        assertFalse(exception.isServerError());
    }

    @Test
    void shouldIdentifyServerError() {
        TransportException exception = new TransportException("Server error", 500, "users", null);
        assertFalse(exception.isClientError());
        assertTrue(exception.isServerError());
    }

    @Test
    void shouldIdentifyNotFound() {
        TransportException exception = new TransportException("Not found", 404, "users", null);
        assertTrue(exception.isNotFound());
        assertTrue(exception.isClientError());
    }

    @Test
    void shouldCreateConnectionFailure() {
        Exception cause = new RuntimeException("Connection refused");
        TransportException exception = TransportException.connectionFailure(
                "Failed to connect",
                cause,
                "users");

        assertEquals("Failed to connect", exception.getMessage());
        assertEquals(0, exception.getStatusCode());
        assertSame(cause, exception.getCause());
    }
}
