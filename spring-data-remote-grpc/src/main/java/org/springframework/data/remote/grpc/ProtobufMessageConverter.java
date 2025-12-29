package org.springframework.data.remote.grpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.remote.grpc.proto.*;
import org.springframework.data.remote.transport.TransportRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for converting between Spring Data Remote transport objects
 * and gRPC protobuf messages.
 */
public class ProtobufMessageConverter {

    private ProtobufMessageConverter() {
        // Utility class
    }

    /**
     * Converts TransportRequest to GetByIdRequest.
     */
    public static GetByIdRequest toGetByIdRequest(TransportRequest request) {
        GetByIdRequest.Builder builder = GetByIdRequest.newBuilder()
                .setResourceName(request.getResourceName())
                .setId(request.getId().orElseThrow(() -> 
                        new IllegalArgumentException("ID is required for FIND_BY_ID operation")).toString());
        
        // Add parameters
        request.getParameters().forEach((key, value) -> 
                builder.putParameters(key, value.toString()));
        
        return builder.build();
    }

    /**
     * Converts TransportRequest to GetAllRequest.
     */
    public static GetAllRequest toGetAllRequest(TransportRequest request) {
        GetAllRequest.Builder builder = GetAllRequest.newBuilder()
                .setResourceName(request.getResourceName());
        
        // Add parameters
        request.getParameters().forEach((key, value) -> 
                builder.putParameters(key, value.toString()));
        
        return builder.build();
    }

    /**
     * Converts TransportRequest to SaveRequest.
     * Serializes the entity payload to JSON.
     */
    public static SaveRequest toSaveRequest(TransportRequest request, ObjectMapper objectMapper) 
            throws JsonProcessingException {
        SaveRequest.Builder builder = SaveRequest.newBuilder()
                .setResourceName(request.getResourceName());
        
        // Add ID if present
        request.getId().ifPresent(id -> builder.setId(id.toString()));
        
        // Serialize entity to JSON
        if (request.getPayload().isPresent()) {
            String entityJson = objectMapper.writeValueAsString(request.getPayload().get());
            builder.setEntityJson(entityJson);
        }
        
        // Add parameters
        request.getParameters().forEach((key, value) -> 
                builder.putParameters(key, value.toString()));
        
        return builder.build();
    }

    /**
     * Converts TransportRequest to DeleteRequest.
     */
    public static DeleteRequest toDeleteRequest(TransportRequest request) {
        DeleteRequest.Builder builder = DeleteRequest.newBuilder()
                .setResourceName(request.getResourceName())
                .setId(request.getId().orElseThrow(() -> 
                        new IllegalArgumentException("ID is required for DELETE operation")).toString());
        
        // Add parameters
        request.getParameters().forEach((key, value) -> 
                builder.putParameters(key, value.toString()));
        
        return builder.build();
    }

    /**
     * Converts TransportRequest to ExistsRequest.
     */
    public static ExistsRequest toExistsRequest(TransportRequest request) {
        ExistsRequest.Builder builder = ExistsRequest.newBuilder()
                .setResourceName(request.getResourceName())
                .setId(request.getId().orElseThrow(() -> 
                        new IllegalArgumentException("ID is required for EXISTS operation")).toString());
        
        // Add parameters
        request.getParameters().forEach((key, value) -> 
                builder.putParameters(key, value.toString()));
        
        return builder.build();
    }

    /**
     * Converts TransportRequest to CountRequest.
     */
    public static CountRequest toCountRequest(TransportRequest request) {
        CountRequest.Builder builder = CountRequest.newBuilder()
                .setResourceName(request.getResourceName());
        
        // Add parameters
        request.getParameters().forEach((key, value) -> 
                builder.putParameters(key, value.toString()));
        
        return builder.build();
    }

    /**
     * Converts EntityResponse to JsonNode.
     * Deserializes the JSON string from the protobuf message.
     */
    public static JsonNode fromEntityResponse(EntityResponse response, ObjectMapper objectMapper) 
            throws IOException {
        if (response.getEntityJson().isEmpty()) {
            return null;
        }
        return objectMapper.readTree(response.getEntityJson());
    }

    /**
     * Converts EntityListResponse to List of JsonNode objects.
     * Deserializes each JSON string from the protobuf message.
     */
    public static List<JsonNode> fromEntityListResponse(EntityListResponse response, ObjectMapper objectMapper) 
            throws IOException {
        List<JsonNode> result = new ArrayList<>();
        for (String entityJson : response.getEntitiesJsonList()) {
            result.add(objectMapper.readTree(entityJson));
        }
        return result;
    }
}
