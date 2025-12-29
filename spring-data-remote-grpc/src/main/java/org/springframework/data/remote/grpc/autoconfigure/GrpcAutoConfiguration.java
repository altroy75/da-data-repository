package org.springframework.data.remote.grpc.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.remote.grpc.GrpcTransportClient;
import org.springframework.data.remote.grpc.GrpcTransportConfig;

/**
 * Spring Boot auto-configuration for gRPC transport.
 * Automatically configures a {@link GrpcTransportClient} bean when:
 * <ul>
 *   <li>gRPC classes are on the classpath</li>
 *   <li>The property {@code spring.data.remote.grpc.host} is set</li>
 *   <li>No custom {@link GrpcTransportClient} bean is already defined</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass(GrpcTransportClient.class)
@ConditionalOnProperty(prefix = "spring.data.remote.grpc", name = "host")
@EnableConfigurationProperties(GrpcTransportConfig.class)
public class GrpcAutoConfiguration {

    /**
     * Creates and configures a GrpcTransportClient bean.
     *
     * @param config       the gRPC configuration properties
     * @param objectMapper Jackson ObjectMapper for JSON serialization
     * @return configured GrpcTransportClient
     */
    @Bean
    @ConditionalOnMissingBean
    public GrpcTransportClient grpcTransportClient(GrpcTransportConfig config, ObjectMapper objectMapper) {
        GrpcTransportClient client = new GrpcTransportClient(objectMapper);
        client.configure(config);
        return client;
    }
}
