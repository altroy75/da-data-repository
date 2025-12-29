package org.springframework.data.remote.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.remote.transport.TransportClientConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for gRPC transport client.
 * Binds to properties with prefix: spring.data.remote.grpc
 */
@ConfigurationProperties(prefix = "spring.data.remote.grpc")
public class GrpcTransportConfig implements TransportClientConfig {

    /**
     * gRPC server hostname.
     */
    private String host;

    /**
     * gRPC server port.
     */
    private int port = 9090;

    /**
     * Whether to use TLS for secure connections.
     */
    private boolean useTls = false;

    /**
     * Maximum inbound message size in bytes.
     * Default: 4MB
     */
    private int maxInboundMessageSize = 4 * 1024 * 1024;

    /**
     * Request deadline (timeout) in milliseconds.
     * Default: 30 seconds
     */
    private long deadlineMs = 30000;

    /**
     * Custom metadata (headers) to include with every request.
     */
    private Map<String, String> metadata = new HashMap<>();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isUseTls() {
        return useTls;
    }

    public void setUseTls(boolean useTls) {
        this.useTls = useTls;
    }

    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public void setMaxInboundMessageSize(int maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
    }

    public long getDeadlineMs() {
        return deadlineMs;
    }

    public void setDeadlineMs(long deadlineMs) {
        this.deadlineMs = deadlineMs;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getBaseUrl() {
        // gRPC doesn't use a traditional URL, so construct host:port
        return host + ":" + port;
    }
}
