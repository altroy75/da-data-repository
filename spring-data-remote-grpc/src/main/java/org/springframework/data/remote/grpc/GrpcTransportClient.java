package org.springframework.data.remote.grpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import org.springframework.data.remote.grpc.proto.*;
import org.springframework.data.remote.transport.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * gRPC implementation of {@link TransportClient}.
 * Communicates with a remote gRPC server using Protocol Buffers.
 * <p>
 * This implementation:
 * <ul>
 *   <li>Serializes entities to JSON strings for transmission</li>
 *   <li>Deserializes responses to {@link JsonNode} objects</li>
 *   <li>Maps CRUD operations to gRPC service methods</li>
 *   <li>Handles errors via gRPC status codes</li>
 * </ul>
 */
public class GrpcTransportClient implements TransportClient<GrpcTransportConfig> {

    private GrpcTransportConfig config;
    private ManagedChannel channel;
    private RemoteDataServiceGrpc.RemoteDataServiceBlockingStub blockingStub;
    private final ObjectMapper objectMapper;

    public GrpcTransportClient() {
        this.objectMapper = new ObjectMapper();
    }

    public GrpcTransportClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure(GrpcTransportConfig config) {
        this.config = config;
        
        // Build channel
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forAddress(config.getHost(), config.getPort())
                .maxInboundMessageSize(config.getMaxInboundMessageSize());
        
        if (!config.isUseTls()) {
            channelBuilder.usePlaintext();
        }
        
        this.channel = channelBuilder.build();
        
        // Create stub
        this.blockingStub = RemoteDataServiceGrpc.newBlockingStub(channel);
        
        // Add custom metadata if provided
        if (!config.getMetadata().isEmpty()) {
            Metadata metadata = new Metadata();
            config.getMetadata().forEach((key, value) -> {
                Metadata.Key<String> metadataKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
                metadata.put(metadataKey, value);
            });
            this.blockingStub = this.blockingStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
        }
        
        // Set deadline
        if (config.getDeadlineMs() > 0) {
            this.blockingStub = blockingStub.withDeadlineAfter(config.getDeadlineMs(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public Class<GrpcTransportConfig> getConfigType() {
        return GrpcTransportConfig.class;
    }

    @Override
    public <T> TransportResponse<T> execute(TransportRequest request, Class<T> responseType) {
        try {
            switch (request.getOperation()) {
                case FIND_BY_ID:
                    return executeFindById(request);
                case SAVE:
                    return executeSave(request);
                case EXISTS:
                    return executeExists(request);
                case DELETE:
                    return executeDelete(request);
                default:
                    throw new TransportException(
                            "Unsupported operation for execute: " + request.getOperation(),
                            0,
                            request.getResourceName(),
                            request.getOperation());
            }
        } catch (StatusRuntimeException e) {
            return handleGrpcError(e);
        } catch (Exception e) {
            throw TransportException.connectionFailure(
                    "gRPC request failed: " + e.getMessage(),
                    e,
                    request.getResourceName());
        }
    }

    @Override
    public <T> TransportResponse<List<T>> executeForList(TransportRequest request, Class<T> elementType) {
        try {
            switch (request.getOperation()) {
                case FIND_ALL:
                case QUERY:
                    return executeFindAll(request);
                default:
                    throw new TransportException(
                            "Unsupported operation for executeForList: " + request.getOperation(),
                            0,
                            request.getResourceName(),
                            request.getOperation());
            }
        } catch (StatusRuntimeException e) {
            return handleGrpcError(e);
        } catch (Exception e) {
            throw TransportException.connectionFailure(
                    "gRPC request failed: " + e.getMessage(),
                    e,
                    request.getResourceName());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> TransportResponse<T> executeFindById(TransportRequest request) throws IOException {
        GetByIdRequest grpcRequest = ProtobufMessageConverter.toGetByIdRequest(request);
        EntityResponse grpcResponse = blockingStub.getById(grpcRequest);
        
        if (!grpcResponse.getSuccess()) {
            return TransportResponse.failure(grpcResponse.getStatusCode(), grpcResponse.getErrorMessage());
        }
        
        JsonNode jsonNode = ProtobufMessageConverter.fromEntityResponse(grpcResponse, objectMapper);
        return (TransportResponse<T>) TransportResponse.<JsonNode>builder()
                .success(true)
                .statusCode(grpcResponse.getStatusCode())
                .body(jsonNode)
                .metadata(grpcResponse.getMetadataMap())
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> TransportResponse<T> executeSave(TransportRequest request) throws IOException {
        SaveRequest grpcRequest = ProtobufMessageConverter.toSaveRequest(request, objectMapper);
        EntityResponse grpcResponse = blockingStub.save(grpcRequest);
        
        if (!grpcResponse.getSuccess()) {
            return TransportResponse.failure(grpcResponse.getStatusCode(), grpcResponse.getErrorMessage());
        }
        
        JsonNode jsonNode = ProtobufMessageConverter.fromEntityResponse(grpcResponse, objectMapper);
        return (TransportResponse<T>) TransportResponse.<JsonNode>builder()
                .success(true)
                .statusCode(grpcResponse.getStatusCode())
                .body(jsonNode)
                .metadata(grpcResponse.getMetadataMap())
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> TransportResponse<T> executeExists(TransportRequest request) {
        ExistsRequest grpcRequest = ProtobufMessageConverter.toExistsRequest(request);
        ExistsResponse grpcResponse = blockingStub.exists(grpcRequest);
        
        if (!grpcResponse.getSuccess()) {
            return TransportResponse.failure(grpcResponse.getStatusCode(), grpcResponse.getErrorMessage());
        }
        
        // Return Boolean wrapped appropriately
        return (TransportResponse<T>) TransportResponse.<Boolean>builder()
                .success(true)
                .statusCode(grpcResponse.getStatusCode())
                .body(grpcResponse.getExists())
                .metadata(grpcResponse.getMetadataMap())
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> TransportResponse<T> executeDelete(TransportRequest request) {
        DeleteRequest grpcRequest = ProtobufMessageConverter.toDeleteRequest(request);
        DeleteResponse grpcResponse = blockingStub.delete(grpcRequest);
        
        if (!grpcResponse.getSuccess()) {
            return TransportResponse.failure(grpcResponse.getStatusCode(), grpcResponse.getErrorMessage());
        }
        
        return (TransportResponse<T>) TransportResponse.empty();
    }

    @SuppressWarnings("unchecked")
    private <T> TransportResponse<List<T>> executeFindAll(TransportRequest request) throws IOException {
        GetAllRequest grpcRequest = ProtobufMessageConverter.toGetAllRequest(request);
        EntityListResponse grpcResponse = blockingStub.getAll(grpcRequest);
        
        if (!grpcResponse.getSuccess()) {
            return TransportResponse.failure(grpcResponse.getStatusCode(), grpcResponse.getErrorMessage());
        }
        
        List<JsonNode> jsonNodes = ProtobufMessageConverter.fromEntityListResponse(grpcResponse, objectMapper);
        return (TransportResponse<List<T>>) (TransportResponse<?>) TransportResponse.<List<JsonNode>>builder()
                .success(true)
                .statusCode(grpcResponse.getStatusCode())
                .body(jsonNodes)
                .metadata(grpcResponse.getMetadataMap())
                .build();
    }

    private <T> TransportResponse<T> handleGrpcError(StatusRuntimeException e) {
        int statusCode = e.getStatus().getCode().value();
        String errorMessage = e.getStatus().getDescription() != null 
                ? e.getStatus().getDescription() 
                : e.getMessage();
        
        return TransportResponse.failure(statusCode, errorMessage);
    }

    /**
     * Shuts down the gRPC channel.
     * Should be called when the client is no longer needed.
     */
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                channel.shutdownNow();
            }
        }
    }
}
