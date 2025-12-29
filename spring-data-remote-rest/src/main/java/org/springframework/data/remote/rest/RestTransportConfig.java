package org.springframework.data.remote.rest;

import org.springframework.data.remote.transport.TransportClientConfig;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for REST transport client.
 * Contains all settings needed for HTTP communication.
 */
public class RestTransportConfig implements TransportClientConfig {

    private final String baseUrl;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final Map<String, String> defaultHeaders;

    private RestTransportConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.defaultHeaders = Collections.unmodifiableMap(new HashMap<>(builder.defaultHeaders));
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public Map<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }

    public static class Builder {
        private String baseUrl = "http://localhost:8080";
        private Duration connectTimeout = Duration.ofSeconds(5);
        private Duration readTimeout = Duration.ofSeconds(30);
        private Map<String, String> defaultHeaders = new HashMap<>();

        private Builder() {
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder header(String name, String value) {
            this.defaultHeaders.put(name, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.defaultHeaders.putAll(headers);
            return this;
        }

        public RestTransportConfig build() {
            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalStateException("Base URL is required");
            }
            return new RestTransportConfig(this);
        }
    }
}
