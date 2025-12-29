package org.springframework.data.remote.rest;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RestTransportConfigTest {

    @Test
    void shouldBuildWithDefaults() {
        RestTransportConfig config = RestTransportConfig.builder()
                .baseUrl("http://localhost:8080")
                .build();

        assertEquals("http://localhost:8080", config.getBaseUrl());
        assertEquals(Duration.ofSeconds(5), config.getConnectTimeout());
        assertEquals(Duration.ofSeconds(30), config.getReadTimeout());
        assertTrue(config.getDefaultHeaders().isEmpty());
    }

    @Test
    void shouldBuildWithCustomValues() {
        RestTransportConfig config = RestTransportConfig.builder()
                .baseUrl("https://api.example.com")
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofMinutes(1))
                .header("Authorization", "Bearer token123")
                .header("X-Api-Key", "key456")
                .build();

        assertEquals("https://api.example.com", config.getBaseUrl());
        assertEquals(Duration.ofSeconds(10), config.getConnectTimeout());
        assertEquals(Duration.ofMinutes(1), config.getReadTimeout());
        assertEquals("Bearer token123", config.getDefaultHeaders().get("Authorization"));
        assertEquals("key456", config.getDefaultHeaders().get("X-Api-Key"));
    }

    @Test
    void shouldThrowWhenBaseUrlMissing() {
        RestTransportConfig.Builder builder = RestTransportConfig.builder()
                .baseUrl(null);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void shouldThrowWhenBaseUrlBlank() {
        RestTransportConfig.Builder builder = RestTransportConfig.builder()
                .baseUrl("   ");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void shouldReturnUnmodifiableHeaders() {
        RestTransportConfig config = RestTransportConfig.builder()
                .baseUrl("http://localhost")
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> config.getDefaultHeaders().put("key", "value"));
    }
}
