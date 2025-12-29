package org.springframework.data.remote.rest.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.remote.rest.RestTransportClient;
import org.springframework.data.remote.rest.RestTransportConfig;
import org.springframework.data.remote.transport.TransportClient;
import org.springframework.web.client.RestClient;

/**
 * Spring Boot auto-configuration for REST transport.
 * Automatically configures a {@link RestTransportClient} when the module is on
 * the classpath.
 */
@AutoConfiguration
@ConditionalOnClass({ RestClient.class, RestTransportClient.class })
@EnableConfigurationProperties(RestTransportProperties.class)
public class RestTransportAutoConfiguration {

    @Autowired
    private RestTransportProperties properties;

    @Bean
    @ConditionalOnMissingBean(RestTransportConfig.class)
    public RestTransportConfig restTransportConfig() {
        RestTransportConfig.Builder builder = RestTransportConfig.builder()
                .baseUrl(properties.getBaseUrl())
                .connectTimeout(properties.getConnectTimeout())
                .readTimeout(properties.getReadTimeout());

        properties.getHeaders().forEach(builder::header);

        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(TransportClient.class)
    public RestTransportClient restTransportClient(RestTransportConfig config,
            @Autowired(required = false) ObjectMapper objectMapper) {
        RestTransportClient client = objectMapper != null
                ? new RestTransportClient(objectMapper)
                : new RestTransportClient();
        client.configure(config);
        return client;
    }
}
