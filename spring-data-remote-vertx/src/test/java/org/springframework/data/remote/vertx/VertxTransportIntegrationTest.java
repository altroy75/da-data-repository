package org.springframework.data.remote.vertx;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.remote.transport.*;
import org.springframework.data.remote.vertx.proto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link VertxTransportClient} with actual event bus.
 * These tests are disabled by default as they require a separate event bus server to be running.
 * Enable and run these tests manually when testing against an actual Vert.x event bus implementation.
 */
@ExtendWith(VertxExtension.class)
class VertxTransportIntegrationTest {

    private VertxTransportClient client;
    private Vertx serverVertx;
    private VertxTransportConfig config;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.serverVertx = vertx;
        
        config = new VertxTransportConfig();
        config.setAddressPrefix("test-data");
        config.setClusteringEnabled(false);
        config.setTimeoutMs(5000);
        
        client = new VertxTransportClient();
        client.configure(config);
        
        // Register mock consumer for testing
        registerMockConsumers(vertx);
        
        testContext.completeNow();
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.shutdown();
        }
    }

    private void registerMockConsumers(Vertx vertx) {
        // Register codecs on server side too
        vertx.eventBus().registerDefaultCodec(GetByIdRequest.class, 
            new ProtobufMessageCodec<>(GetByIdRequest.class, "GetByIdRequestCodec"));
        vertx.eventBus().registerDefaultCodec(EntityResponse.class, 
            new ProtobufMessageCodec<>(EntityResponse.class, "EntityResponseCodec"));
        vertx.eventBus().registerDefaultCodec(GetAllRequest.class, 
            new ProtobufMessageCodec<>(GetAllRequest.class, "GetAllRequestCodec"));
        vertx.eventBus().registerDefaultCodec(EntityListResponse.class, 
            new ProtobufMessageCodec<>(EntityListResponse.class, "EntityListResponseCodec"));
        vertx.eventBus().registerDefaultCodec(ExistsRequest.class, 
            new ProtobufMessageCodec<>(ExistsRequest.class, "ExistsRequestCodec"));
        vertx.eventBus().registerDefaultCodec(ExistsResponse.class, 
            new ProtobufMessageCodec<>(ExistsResponse.class, "ExistsResponseCodec"));
        vertx.eventBus().registerDefaultCodec(DeleteRequest.class, 
            new ProtobufMessageCodec<>(DeleteRequest.class, "DeleteRequestCodec"));
        vertx.eventBus().registerDefaultCodec(DeleteResponse.class, 
            new ProtobufMessageCodec<>(DeleteResponse.class, "DeleteResponseCodec"));
        vertx.eventBus().registerDefaultCodec(CountRequest.class, 
            new ProtobufMessageCodec<>(CountRequest.class, "CountRequestCodec"));
        vertx.eventBus().registerDefaultCodec(CountResponse.class, 
            new ProtobufMessageCodec<>(CountResponse.class, "CountResponseCodec"));
        
        // Mock GET_BY_ID handler
        vertx.eventBus().<GetByIdRequest>consumer("test-data.get-by-id", message -> {
            GetByIdRequest request = message.body();
            String responseJson = "{\"id\":\"" + request.getId() + "\",\"name\":\"Test User\"}";
            
            EntityResponse response = EntityResponse.newBuilder()
                .setSuccess(true)
                .setStatusCode(200)
                .setEntityJson(responseJson)
                .build();
            
            message.reply(response);
        });
        
        // Mock GET_ALL handler
        vertx.eventBus().<GetAllRequest>consumer("test-data.get-all", message -> {
            EntityListResponse response = EntityListResponse.newBuilder()
                .setSuccess(true)
                .setStatusCode(200)
                .addEntitiesJson("{\"id\":\"1\",\"name\":\"User 1\"}")
                .addEntitiesJson("{\"id\":\"2\",\"name\":\"User 2\"}")
                .build();
            
            message.reply(response);
        });
        
        // Mock EXISTS handler
        vertx.eventBus().<ExistsRequest>consumer("test-data.exists", message -> {
            ExistsResponse response = ExistsResponse.newBuilder()
                .setSuccess(true)
                .setStatusCode(200)
                .setExists(true)
                .build();
            
            message.reply(response);
        });
        
        // Mock DELETE handler
        vertx.eventBus().<DeleteRequest>consumer("test-data.delete", message -> {
            DeleteResponse response = DeleteResponse.newBuilder()
                .setSuccess(true)
                .setStatusCode(204)
                .build();
            
            message.reply(response);
        });
        
        // Mock COUNT handler
        vertx.eventBus().<CountRequest>consumer("test-data.count", message -> {
            CountResponse response = CountResponse.newBuilder()
                .setSuccess(true)
                .setStatusCode(200)
                .setCount(42)
                .build();
            
            message.reply(response);
        });
    }

    @Test
    @Disabled("Requires actual event bus server running")
    void testFindById() {
        TransportRequest request = TransportRequest.builder()
            .operation(TransportOperation.FIND_BY_ID)
            .resourceName("/users")
            .id("123")
            .build();
        
        TransportResponse<Map> response = client.execute(request, Map.class);
        
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody().orElse(null));
        assertEquals("123", response.getBody().get().get("id"));
        assertEquals("Test User", response.getBody().get().get("name"));
    }

    @Test
    @Disabled("Requires actual event bus server running")
    void testFindAll() {
        TransportRequest request = TransportRequest.builder()
            .operation(TransportOperation.FIND_ALL)
            .resourceName("/users")
            .build();
        
        TransportResponse<List<Map>> response = client.executeForList(request, Map.class);
        
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody().orElse(null));
        assertEquals(2, response.getBody().get().size());
    }

    @Test
    @Disabled("Requires actual event bus server running")
    void testExists() {
        TransportRequest request = TransportRequest.builder()
            .operation(TransportOperation.EXISTS)
            .resourceName("/users")
            .id("123")
            .build();
        
        TransportResponse<Boolean> response = client.execute(request, Boolean.class);
        
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().orElse(false));
    }

    @Test
    @Disabled("Requires actual event bus server running")
    void testDelete() {
        TransportRequest request = TransportRequest.builder()
            .operation(TransportOperation.DELETE)
            .resourceName("/users")
            .id("123")
            .build();
        
        TransportResponse<Void> response = client.execute(request, Void.class);
        
        assertTrue(response.isSuccess());
        assertEquals(204, response.getStatusCode());
    }

    @Test
    @Disabled("Requires actual event bus server running")
    void testCount() {
        TransportRequest request = TransportRequest.builder()
            .operation(TransportOperation.COUNT)
            .resourceName("/users")
            .build();
        
        TransportResponse<Long> response = client.execute(request, Long.class);
        
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertEquals(42L, response.getBody().orElse(0L));
    }
}
