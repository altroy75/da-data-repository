package org.springframework.data.remote.vertx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import org.springframework.data.remote.transport.*;
import org.springframework.data.remote.vertx.proto.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Vert.x event bus implementation of {@link TransportClient}.
 * Communicates with remote services using Vert.x clustered event bus and Protocol Buffers.
 *
 * <p>
 * This implementation:
 * <ul>
 *   <li>Uses Vert.x event bus for messaging (supports clustering)</li>
 *   <li>Serializes entities to JSON strings for transmission</li>
 *   <li>Deserializes responses to {@link JsonNode} objects</li>
 *   <li>Maps CRUD operations to event bus addresses</li>
 *   <li>Handles errors via response status codes</li>
 * </ul>
 */
public class VertxTransportClient implements TransportClient<VertxTransportConfig> {

    private Vertx vertx;
    private EventBus eventBus;
    private VertxTransportConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(VertxTransportConfig config) {
        this.config = config;
        
        // Create Vert.x options
        VertxOptions options = new VertxOptions();
        if (config.getEventLoopPoolSize() != null) {
            options.setEventLoopPoolSize(config.getEventLoopPoolSize());
        }
        if (config.getWorkerPoolSize() != null) {
            options.setWorkerPoolSize(config.getWorkerPoolSize());
        }

        // Create Vert.x instance (clustered or non-clustered)
        if (config.isClusteringEnabled()) {
            CompletableFuture<Vertx> future = new CompletableFuture<>();
            
            if (config.getClusterHost() != null) {
                options.getEventBusOptions()
                    .setHost(config.getClusterHost())
                    .setPort(config.getClusterPort());
            }
            
            Vertx.clusteredVertx(options, ar -> {
                if (ar.succeeded()) {
                    future.complete(ar.result());
                } else {
                    future.completeExceptionally(ar.cause());
                }
            });
            
            try {
                this.vertx = future.get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create clustered Vert.x instance", e);
            }
        } else {
            this.vertx = Vertx.vertx(options);
        }
        
        this.eventBus = vertx.eventBus();
        registerCodecs();
    }

    private void registerCodecs() {
        // Register Protobuf message codecs
        eventBus.registerDefaultCodec(GetByIdRequest.class, 
            new ProtobufMessageCodec<>(GetByIdRequest.class, "GetByIdRequestCodec"));
        eventBus.registerDefaultCodec(GetAllRequest.class, 
            new ProtobufMessageCodec<>(GetAllRequest.class, "GetAllRequestCodec"));
        eventBus.registerDefaultCodec(SaveRequest.class, 
            new ProtobufMessageCodec<>(SaveRequest.class, "SaveRequestCodec"));
        eventBus.registerDefaultCodec(DeleteRequest.class, 
            new ProtobufMessageCodec<>(DeleteRequest.class, "DeleteRequestCodec"));
        eventBus.registerDefaultCodec(ExistsRequest.class, 
            new ProtobufMessageCodec<>(ExistsRequest.class, "ExistsRequestCodec"));
        eventBus.registerDefaultCodec(CountRequest.class, 
            new ProtobufMessageCodec<>(CountRequest.class, "CountRequestCodec"));
        
        eventBus.registerDefaultCodec(EntityResponse.class, 
            new ProtobufMessageCodec<>(EntityResponse.class, "EntityResponseCodec"));
        eventBus.registerDefaultCodec(EntityListResponse.class, 
            new ProtobufMessageCodec<>(EntityListResponse.class, "EntityListResponseCodec"));
        eventBus.registerDefaultCodec(DeleteResponse.class, 
            new ProtobufMessageCodec<>(DeleteResponse.class, "DeleteResponseCodec"));
        eventBus.registerDefaultCodec(ExistsResponse.class, 
            new ProtobufMessageCodec<>(ExistsResponse.class, "ExistsResponseCodec"));
        eventBus.registerDefaultCodec(CountResponse.class, 
            new ProtobufMessageCodec<>(CountResponse.class, "CountResponseCodec"));
    }

    @Override
    public Class<VertxTransportConfig> getConfigType() {
        return VertxTransportConfig.class;
    }

    @Override
    public <T> TransportResponse<T> execute(TransportRequest request, Class<T> responseType) {
        TransportOperation operation = request.getOperation();
        
        switch (operation) {
            case FIND_BY_ID:
                return executeFindById(request, responseType);
            case SAVE:
                return executeSave(request, responseType);
            case EXISTS:
                return executeExists(request);
            case DELETE:
                return executeDelete(request);
            case COUNT:
                return executeCount(request);
            default:
                throw new UnsupportedOperationException("Operation not supported: " + operation);
        }
    }

    @Override
    public <T> TransportResponse<List<T>> executeForList(TransportRequest request, Class<T> elementType) {
        if (request.getOperation() != TransportOperation.FIND_ALL) {
            throw new UnsupportedOperationException("executeForList only supports FIND_ALL operation");
        }
        return executeFindAll(request, elementType);
    }

    private <T> TransportResponse<T> executeFindById(TransportRequest request, Class<T> responseType) {
        String address = buildAddress("get-by-id");
        
        GetByIdRequest.Builder builder = GetByIdRequest.newBuilder()
            .setResourceName(request.getResourceName())
            .setId(String.valueOf(request.getId().orElse("")));
        
        if (request.getParameters() != null) {
            builder.putAllParameters(convertParameters(request.getParameters()));
        }
        
        GetByIdRequest protoRequest = builder.build();
        
        CompletableFuture<EntityResponse> future = new CompletableFuture<>();
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(config.getTimeoutMs());
        
        eventBus.<EntityResponse>request(address, protoRequest, options, ar -> {
            if (ar.succeeded()) {
                future.complete(ar.result().body());
            } else {
                future.completeExceptionally(ar.cause());
            }
        });
        
        try {
            EntityResponse response = future.get(config.getTimeoutMs(), TimeUnit.MILLISECONDS);
            return parseEntityResponse(response, responseType);
        } catch (Exception e) {
            throw TransportException.connectionFailure(
                "Failed to execute FIND_BY_ID: " + e.getMessage(), 
                e, 
                request.getResourceName());
        }
    }

    private <T> TransportResponse<T> executeSave(TransportRequest request, Class<T> responseType) {
        String address = buildAddress("save");
        
        String entityJson;
        try {
            entityJson = objectMapper.writeValueAsString(request.getPayload().orElse(null));
        } catch (IOException e) {
            throw TransportException.connectionFailure(
                "Failed to serialize entity: " + e.getMessage(), 
                e, 
                request.getResourceName());
        }
        
        SaveRequest.Builder builder = SaveRequest.newBuilder()
            .setResourceName(request.getResourceName())
            .setEntityJson(entityJson);
        
        if (request.getId().isPresent()) {
            builder.setId(String.valueOf(request.getId().get()));
        }
        
        if (request.getParameters() != null) {
            builder.putAllParameters(convertParameters(request.getParameters()));
        }
        
        SaveRequest protoRequest = builder.build();
        
        CompletableFuture<EntityResponse> future = new CompletableFuture<>();
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(config.getTimeoutMs());
        
        eventBus.<EntityResponse>request(address, protoRequest, options, ar -> {
            if (ar.succeeded()) {
                future.complete(ar.result().body());
            } else {
                future.completeExceptionally(ar.cause());
            }
        });
        
        try {
            EntityResponse response = future.get(config.getTimeoutMs(), TimeUnit.MILLISECONDS);
            return parseEntityResponse(response, responseType);
        } catch (Exception e) {
            throw TransportException.connectionFailure(
                "Failed to execute SAVE: " + e.getMessage(), 
                e, 
                request.getResourceName());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> TransportResponse<T> executeExists(TransportRequest request) {
        String address = buildAddress("exists");
        
        ExistsRequest.Builder builder = ExistsRequest.newBuilder()
            .setResourceName(request.getResourceName())
            .setId(String.valueOf(request.getId().orElse("")));
        
        if (request.getParameters() != null) {
            builder.putAllParameters(convertParameters(request.getParameters()));
        }
        
        ExistsRequest protoRequest = builder.build();
        
        CompletableFuture<ExistsResponse> future = new CompletableFuture<>();
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(config.getTimeoutMs());
        
        eventBus.<ExistsResponse>request(address, protoRequest, options, ar -> {
            if (ar.succeeded()) {
                future.complete(ar.result().body());
            } else {
                future.completeExceptionally(ar.cause());
            }
        });
        
        try {
            ExistsResponse response = future.get(config.getTimeoutMs(), TimeUnit.MILLISECONDS);
            return TransportResponse.<T>builder()
                .success(response.getSuccess())
                .statusCode(response.getStatusCode())
                .body((T) Boolean.valueOf(response.getExists()))
                .errorMessage(response.getErrorMessage().isEmpty() ? null : response.getErrorMessage())
                .metadata(new HashMap<>(response.getMetadataMap()))
                .build();
        } catch (Exception e) {
            throw TransportException.connectionFailure(
                "Failed to execute EXISTS: " + e.getMessage(), 
                e, 
                request.getResourceName());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> TransportResponse<T> executeDelete(TransportRequest request) {
        String address = buildAddress("delete");
        
        DeleteRequest.Builder builder = DeleteRequest.newBuilder()
            .setResourceName(request.getResourceName())
            .setId(String.valueOf(request.getId().orElse("")));
        
        if (request.getParameters() != null) {
            builder.putAllParameters(convertParameters(request.getParameters()));
        }
        
        DeleteRequest protoRequest = builder.build();
        
        CompletableFuture<DeleteResponse> future = new CompletableFuture<>();
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(config.getTimeoutMs());
        
        eventBus.<DeleteResponse>request(address, protoRequest, options, ar -> {
            if (ar.succeeded()) {
                future.complete(ar.result().body());
            } else {
                future.completeExceptionally(ar.cause());
            }
        });
        
        try {
            DeleteResponse response = future.get(config.getTimeoutMs(), TimeUnit.MILLISECONDS);
            return TransportResponse.<T>builder()
                .success(response.getSuccess())
                .statusCode(response.getStatusCode())
                .errorMessage(response.getErrorMessage().isEmpty() ? null : response.getErrorMessage())
                .metadata(new HashMap<>(response.getMetadataMap()))
                .build();
        } catch (Exception e) {
            throw TransportException.connectionFailure(
                "Failed to execute DELETE: " + e.getMessage(), 
                e, 
                request.getResourceName());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> TransportResponse<T> executeCount(TransportRequest request) {
        String address = buildAddress("count");
        
        CountRequest.Builder builder = CountRequest.newBuilder()
            .setResourceName(request.getResourceName());
        
        if (request.getParameters() != null) {
            builder.putAllParameters(convertParameters(request.getParameters()));
        }
        
        CountRequest protoRequest = builder.build();
        
        CompletableFuture<CountResponse> future = new CompletableFuture<>();
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(config.getTimeoutMs());
        
        eventBus.<CountResponse>request(address, protoRequest, options, ar -> {
            if (ar.succeeded()) {
                future.complete(ar.result().body());
            } else {
                future.completeExceptionally(ar.cause());
            }
        });
        
        try {
            CountResponse response = future.get(config.getTimeoutMs(), TimeUnit.MILLISECONDS);
            return TransportResponse.<T>builder()
                .success(response.getSuccess())
                .statusCode(response.getStatusCode())
                .body((T) Long.valueOf(response.getCount()))
                .errorMessage(response.getErrorMessage().isEmpty() ? null : response.getErrorMessage())
                .metadata(new HashMap<>(response.getMetadataMap()))
                .build();
        } catch (Exception e) {
            throw TransportException.connectionFailure(
                "Failed to execute COUNT: " + e.getMessage(), 
                e, 
                request.getResourceName());
        }
    }

    private <T> TransportResponse<List<T>> executeFindAll(TransportRequest request, Class<T> elementType) {
        String address = buildAddress("get-all");
        
        GetAllRequest.Builder builder = GetAllRequest.newBuilder()
            .setResourceName(request.getResourceName());
        
        if (request.getParameters() != null) {
            builder.putAllParameters(convertParameters(request.getParameters()));
        }
        
        GetAllRequest protoRequest = builder.build();
        
        CompletableFuture<EntityListResponse> future = new CompletableFuture<>();
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(config.getTimeoutMs());
        
        eventBus.<EntityListResponse>request(address, protoRequest, options, ar -> {
            if (ar.succeeded()) {
                future.complete(ar.result().body());
            } else {
                future.completeExceptionally(ar.cause());
            }
        });
        
        try {
            EntityListResponse response = future.get(config.getTimeoutMs(), TimeUnit.MILLISECONDS);
            return parseEntityListResponse(response, elementType);
        } catch (Exception e) {
            throw TransportException.connectionFailure(
                "Failed to execute FIND_ALL: " + e.getMessage(), 
                e, 
                request.getResourceName());
        }
    }

    private <T> TransportResponse<T> parseEntityResponse(EntityResponse response, Class<T> responseType) {
        T entity = null;
        
        if (response.getSuccess() && !response.getEntityJson().isEmpty()) {
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getEntityJson());
                entity = objectMapper.treeToValue(jsonNode, responseType);
            } catch (IOException e) {
                throw TransportException.connectionFailure(
                    "Failed to parse entity response: " + e.getMessage(), 
                    e, 
                    "unknown");
            }
        }
        
        return TransportResponse.<T>builder()
            .success(response.getSuccess())
            .statusCode(response.getStatusCode())
            .body(entity)
            .errorMessage(response.getErrorMessage().isEmpty() ? null : response.getErrorMessage())
            .metadata(new HashMap<>(response.getMetadataMap()))
            .build();
    }

    private <T> TransportResponse<List<T>> parseEntityListResponse(EntityListResponse response, Class<T> elementType) {
        List<T> entities = new ArrayList<>();
        
        if (response.getSuccess()) {
            for (String entityJson : response.getEntitiesJsonList()) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(entityJson);
                    T entity = objectMapper.treeToValue(jsonNode, elementType);
                    entities.add(entity);
                } catch (IOException e) {
                    throw TransportException.connectionFailure(
                        "Failed to parse entity in list response: " + e.getMessage(), 
                        e, 
                        "unknown");
                }
            }
        }
        
        return TransportResponse.<List<T>>builder()
            .success(response.getSuccess())
            .statusCode(response.getStatusCode())
            .body(entities)
            .errorMessage(response.getErrorMessage().isEmpty() ? null : response.getErrorMessage())
            .metadata(new HashMap<>(response.getMetadataMap()))
            .build();
    }

    private Map<String, String> convertParameters(Map<String, Object> parameters) {
        Map<String, String> result = new HashMap<>();
        if (parameters != null) {
            parameters.forEach((key, value) -> 
                result.put(key, value != null ? String.valueOf(value) : ""));
        }
        return result;
    }

    private String buildAddress(String operation) {
        return config.getAddressPrefix() + "." + operation;
    }

    /**
     * Shuts down the Vert.x instance.
     * Should be called when the client is no longer needed.
     */
    public void shutdown() {
        if (vertx != null) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            vertx.close(ar -> {
                if (ar.succeeded()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(ar.cause());
                }
            });
            
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                // Log but don't throw - we're shutting down anyway
                System.err.println("Error during Vert.x shutdown: " + e.getMessage());
            }
        }
    }
}
