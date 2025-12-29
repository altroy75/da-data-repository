package org.springframework.data.remote.vertx.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.remote.vertx.VertxTransportClient;
import org.springframework.data.remote.vertx.VertxTransportConfig;

import jakarta.annotation.PreDestroy;

/**
 * Spring Boot auto-configuration for Vert.x transport client.
 * Activates when spring.data.remote.vertx.address-prefix is configured.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.data.remote.vertx", name = "address-prefix")
@EnableConfigurationProperties(VertxTransportConfig.class)
public class VertxAutoConfiguration {

    private VertxTransportClient transportClient;

    @Bean
    @ConditionalOnMissingBean
    public VertxTransportClient vertxTransportClient(VertxTransportConfig config) {
        VertxTransportClient client = new VertxTransportClient();
        client.configure(config);
        this.transportClient = client;
        return client;
    }

    @PreDestroy
    public void shutdown() {
        if (transportClient != null) {
            transportClient.shutdown();
        }
    }
}
