package org.springframework.data.remote.rest.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for REST transport.
 * These can be set in application.yml or application.properties.
 *
 * <p>
 * Example application.yml:
 * 
 * <pre>
 * spring:
 *   data:
 *     remote:
 *       rest:
 *         base-url: https://api.example.com
 *         connect-timeout: 5s
 *         read-timeout: 30s
 *         headers:
 *           Authorization: Bearer token123
 * </pre>
 */
@ConfigurationProperties(prefix = "spring.data.remote.rest")
public class RestTransportProperties {

    /**
     * Base URL for the remote API.
     */
    private String baseUrl = "http://localhost:8080";

    /**
     * Connection timeout for HTTP requests.
     */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /**
     * Read timeout for HTTP requests.
     */
    private Duration readTimeout = Duration.ofSeconds(30);

    /**
     * Default headers to include in all requests.
     */
    private Map<String, String> headers = new HashMap<>();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
