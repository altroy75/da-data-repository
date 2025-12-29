package org.springframework.data.remote.vertx;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.remote.transport.TransportClientConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Vert.x event bus transport client.
 * Binds to properties with prefix: spring.data.remote.vertx
 */
@ConfigurationProperties(prefix = "spring.data.remote.vertx")
public class VertxTransportConfig implements TransportClientConfig {

    /**
     * Event bus address prefix for remote data operations.
     * Default: "remote-data"
     */
    private String addressPrefix = "remote-data";

    /**
     * Whether to enable clustering for distributed deployments.
     * Default: false
     */
    private boolean clusteringEnabled = false;

    /**
     * Cluster host for joining the cluster.
     * Only used when clustering is enabled.
     */
    private String clusterHost;

    /**
     * Cluster port for joining the cluster.
     * Only used when clustering is enabled.
     * Default: 0 (auto-assign)
     */
    private int clusterPort = 0;

    /**
     * Request timeout in milliseconds.
     * Default: 30 seconds
     */
    private long timeoutMs = 30000;

    /**
     * Custom headers/metadata to include with every request.
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * Number of event loop threads.
     * Default: 2 * number of CPU cores
     */
    private Integer eventLoopPoolSize;

    /**
     * Number of worker threads.
     * Default: 20
     */
    private Integer workerPoolSize;

    public String getAddressPrefix() {
        return addressPrefix;
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }

    public boolean isClusteringEnabled() {
        return clusteringEnabled;
    }

    public void setClusteringEnabled(boolean clusteringEnabled) {
        this.clusteringEnabled = clusteringEnabled;
    }

    public String getClusterHost() {
        return clusterHost;
    }

    public void setClusterHost(String clusterHost) {
        this.clusterHost = clusterHost;
    }

    public int getClusterPort() {
        return clusterPort;
    }

    public void setClusterPort(int clusterPort) {
        this.clusterPort = clusterPort;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Integer getEventLoopPoolSize() {
        return eventLoopPoolSize;
    }

    public void setEventLoopPoolSize(Integer eventLoopPoolSize) {
        this.eventLoopPoolSize = eventLoopPoolSize;
    }

    public Integer getWorkerPoolSize() {
        return workerPoolSize;
    }

    public void setWorkerPoolSize(Integer workerPoolSize) {
        this.workerPoolSize = workerPoolSize;
    }

    @Override
    public String getBaseUrl() {
        // For Vert.x event bus, return the address prefix
        return addressPrefix;
    }
}
