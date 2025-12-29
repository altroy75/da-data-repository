package org.springframework.data.remote.transport;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TransportRequestTest {

    @Test
    void shouldBuildRequestWithAllParameters() {
        TransportRequest request = TransportRequest.builder()
                .operation(TransportOperation.FIND_BY_ID)
                .resourceName("users")
                .id(123L)
                .entityType(Object.class)
                .parameter("expand", "profile")
                .build();

        assertEquals(TransportOperation.FIND_BY_ID, request.getOperation());
        assertEquals("users", request.getResourceName());
        assertTrue(request.getId().isPresent());
        assertEquals(123L, request.getId().get());
        assertEquals("profile", request.getParameters().get("expand"));
    }

    @Test
    void shouldBuildRequestWithPayload() {
        Object payload = new Object();
        TransportRequest request = TransportRequest.builder()
                .operation(TransportOperation.SAVE)
                .resourceName("users")
                .payload(payload)
                .build();

        assertTrue(request.getPayload().isPresent());
        assertSame(payload, request.getPayload().get());
    }

    @Test
    void shouldThrowWhenOperationMissing() {
        TransportRequest.Builder builder = TransportRequest.builder()
                .resourceName("users");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void shouldThrowWhenResourceNameMissing() {
        TransportRequest.Builder builder = TransportRequest.builder()
                .operation(TransportOperation.FIND_ALL);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void shouldReturnUnmodifiableParameters() {
        TransportRequest request = TransportRequest.builder()
                .operation(TransportOperation.FIND_ALL)
                .resourceName("users")
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> request.getParameters().put("key", "value"));
    }
}
