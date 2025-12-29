package org.springframework.data.remote.vertx;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.remote.transport.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link VertxTransportClient}.
 */
class VertxTransportClientTest {

    private VertxTransportClient client;
    private VertxTransportConfig config;

    @BeforeEach
    void setUp() {
        config = new VertxTransportConfig();
        config.setAddressPrefix("test-remote-data");
        config.setClusteringEnabled(false);
        config.setTimeoutMs(5000);
        
        client = new VertxTransportClient();
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.shutdown();
        }
    }

    @Test
    void testConfigureNonClustered() {
        assertDoesNotThrow(() -> client.configure(config));
    }

    @Test
    void testGetConfigType() {
        assertEquals(VertxTransportConfig.class, client.getConfigType());
    }

    @Test
    void testConfigurationProperties() {
        config.setAddressPrefix("custom-prefix");
        config.setTimeoutMs(10000);
        config.setEventLoopPoolSize(4);
        config.setWorkerPoolSize(10);
        
        assertEquals("custom-prefix", config.getAddressPrefix());
        assertEquals(10000, config.getTimeoutMs());
        assertEquals(4, config.getEventLoopPoolSize());
        assertEquals(10, config.getWorkerPoolSize());
    }

    @Test
    void testClusterConfiguration() {
        config.setClusteringEnabled(true);
        config.setClusterHost("localhost");
        config.setClusterPort(5701);
        
        assertTrue(config.isClusteringEnabled());
        assertEquals("localhost", config.getClusterHost());
        assertEquals(5701, config.getClusterPort());
    }

    @Test
    void testBaseUrl() {
        config.setAddressPrefix("my-service");
        assertEquals("my-service", config.getBaseUrl());
    }
}
