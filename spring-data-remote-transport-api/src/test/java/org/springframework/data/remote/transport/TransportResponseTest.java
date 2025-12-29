package org.springframework.data.remote.transport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransportResponseTest {

    @Test
    void shouldCreateSuccessResponse() {
        TransportResponse<String> response = TransportResponse.success("data");

        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().isPresent());
        assertEquals("data", response.getBody().get());
        assertTrue(response.getErrorMessage().isEmpty());
    }

    @Test
    void shouldCreateEmptyResponse() {
        TransportResponse<Void> response = TransportResponse.empty();

        assertTrue(response.isSuccess());
        assertEquals(204, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void shouldCreateFailureResponse() {
        TransportResponse<String> response = TransportResponse.failure(404, "Not Found");

        assertFalse(response.isSuccess());
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        assertEquals("Not Found", response.getErrorMessage().get());
    }

    @Test
    void shouldBuildWithMetadata() {
        TransportResponse<String> response = TransportResponse.<String>builder()
                .body("data")
                .success(true)
                .statusCode(200)
                .metadata("X-Total-Count", "100")
                .build();

        assertEquals("100", response.getMetadata().get("X-Total-Count"));
    }

    @Test
    void shouldReturnUnmodifiableMetadata() {
        TransportResponse<String> response = TransportResponse.success("data");

        assertThrows(UnsupportedOperationException.class,
                () -> response.getMetadata().put("key", "value"));
    }
}
