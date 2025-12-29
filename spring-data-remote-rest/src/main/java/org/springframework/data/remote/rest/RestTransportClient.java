package org.springframework.data.remote.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.remote.transport.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * REST implementation of {@link TransportClient}.
 * Uses Spring's RestClient for HTTP communication.
 *
 * <p>
 * This implementation maps transport operations to HTTP methods:
 * <ul>
 * <li>FIND_BY_ID → GET /resource/{id}</li>
 * <li>FIND_ALL → GET /resource</li>
 * <li>SAVE → POST /resource (new) or PUT /resource/{id} (existing)</li>
 * <li>DELETE → DELETE /resource/{id}</li>
 * <li>EXISTS → HEAD /resource/{id}</li>
 * <li>COUNT → GET /resource/count</li>
 * </ul>
 */
public class RestTransportClient implements TransportClient<RestTransportConfig> {

    private RestClient restClient;
    private RestTransportConfig config;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new REST transport client with default ObjectMapper.
     */
    public RestTransportClient() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    /**
     * Creates a new REST transport client with custom ObjectMapper.
     *
     * @param objectMapper the ObjectMapper to use for JSON serialization
     */
    public RestTransportClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new REST transport client with configuration.
     *
     * @param config the REST transport configuration
     */
    public RestTransportClient(RestTransportConfig config) {
        this();
        configure(config);
    }

    @Override
    public void configure(RestTransportConfig config) {
        this.config = config;

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(config.getBaseUrl());

        // Add default headers
        config.getDefaultHeaders().forEach(builder::defaultHeader);

        // Add default content type
        builder.defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        builder.defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE);

        this.restClient = builder.build();
    }

    @Override
    public Class<RestTransportConfig> getConfigType() {
        return RestTransportConfig.class;
    }

    @Override
    public <T> TransportResponse<T> execute(TransportRequest request, Class<T> responseType) {
        try {
            String uri = buildUri(request);
            HttpMethod method = mapOperationToMethod(request.getOperation(), request.getId().isPresent());

            RestClient.RequestBodySpec requestSpec = restClient
                    .method(method)
                    .uri(uri);

            // Add payload for write operations
            if (request.getPayload().isPresent()) {
                requestSpec.body(request.getPayload().get());
            }

            // Handle EXISTS operation specially
            if (request.getOperation() == TransportOperation.EXISTS) {
                return handleExistsRequest(requestSpec, request);
            }

            // Handle void responses (delete)
            if (responseType == Void.class || responseType == void.class) {
                requestSpec.retrieve().toBodilessEntity();
                return TransportResponse.empty();
            }

            // Execute and parse response
            String responseBody = requestSpec.retrieve().body(String.class);
            T result = parseResponse(responseBody, responseType);

            return TransportResponse.success(result);

        } catch (HttpClientErrorException e) {
            return handleHttpError(e, request);
        } catch (HttpServerErrorException e) {
            return handleHttpError(e, request);
        } catch (RestClientException e) {
            throw TransportException.connectionFailure(
                    "Failed to connect to remote service: " + e.getMessage(),
                    e,
                    request.getResourceName());
        }
    }

    @Override
    public <T> TransportResponse<List<T>> executeForList(TransportRequest request, Class<T> elementType) {
        try {
            String uri = buildUri(request);

            String responseBody = restClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            JavaType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, elementType);
            List<T> result = objectMapper.readValue(responseBody, listType);

            return TransportResponse.success(result);

        } catch (HttpClientErrorException e) {
            return handleHttpError(e, request);
        } catch (HttpServerErrorException e) {
            return handleHttpError(e, request);
        } catch (RestClientException e) {
            throw TransportException.connectionFailure(
                    "Failed to connect to remote service: " + e.getMessage(),
                    e,
                    request.getResourceName());
        } catch (JsonProcessingException e) {
            throw new TransportException(
                    "Failed to parse response: " + e.getMessage(),
                    e,
                    0,
                    request.getResourceName(),
                    request.getOperation());
        }
    }

    /**
     * Builds the URI for the request based on operation and parameters.
     */
    private String buildUri(TransportRequest request) {
        StringBuilder uri = new StringBuilder();

        // Add resource path
        String resourceName = request.getResourceName();
        if (!resourceName.startsWith("/")) {
            uri.append("/");
        }
        uri.append(resourceName);

        // Add ID for single-entity operations
        if (request.getId().isPresent()) {
            uri.append("/").append(request.getId().get());
        }

        // Add count suffix for COUNT operation
        if (request.getOperation() == TransportOperation.COUNT) {
            uri.append("/count");
        }

        // Add query parameters
        Map<String, Object> params = request.getParameters();
        if (!params.isEmpty()) {
            uri.append("?");
            params.forEach((key, value) -> {
                uri.append(key).append("=").append(value).append("&");
            });
            // Remove trailing &
            uri.setLength(uri.length() - 1);
        }

        return uri.toString();
    }

    /**
     * Maps transport operations to HTTP methods.
     */
    private HttpMethod mapOperationToMethod(TransportOperation operation, boolean hasId) {
        return switch (operation) {
            case FIND_BY_ID, FIND_ALL, QUERY, COUNT -> HttpMethod.GET;
            case SAVE -> hasId ? HttpMethod.PUT : HttpMethod.POST;
            case DELETE -> HttpMethod.DELETE;
            case EXISTS -> HttpMethod.HEAD;
        };
    }

    /**
     * Handles EXISTS operation using HEAD request.
     */
    @SuppressWarnings("unchecked")
    private <T> TransportResponse<T> handleExistsRequest(RestClient.RequestBodySpec requestSpec,
            TransportRequest request) {
        try {
            requestSpec.retrieve().toBodilessEntity();
            return (TransportResponse<T>) TransportResponse.success(Boolean.TRUE);
        } catch (HttpClientErrorException.NotFound e) {
            return (TransportResponse<T>) TransportResponse.success(Boolean.FALSE);
        }
    }

    /**
     * Parses JSON response to the target type.
     */
    private <T> T parseResponse(String responseBody, Class<T> responseType) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(responseBody, responseType);
        } catch (JsonProcessingException e) {
            throw new TransportException(
                    "Failed to parse response: " + e.getMessage(),
                    e,
                    200,
                    null,
                    null);
        }
    }

    /**
     * Handles HTTP errors and converts them to TransportResponse.
     */
    private <T> TransportResponse<T> handleHttpError(HttpClientErrorException e, TransportRequest request) {
        return TransportResponse.failure(
                e.getStatusCode().value(),
                e.getStatusText() + ": " + e.getResponseBodyAsString());
    }

    private <T> TransportResponse<T> handleHttpError(HttpServerErrorException e, TransportRequest request) {
        return TransportResponse.failure(
                e.getStatusCode().value(),
                e.getStatusText() + ": " + e.getResponseBodyAsString());
    }
}
