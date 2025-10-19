package top.codelong.sendsdk.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.codelong.sendsdk.client.GatewayClient;
import top.codelong.sendsdk.utils.JwtUtils;

@Configuration
@EnableConfigurationProperties(GatewaySDKProperties.class)
public class GatewayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils() {
        return new JwtUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public CloseableHttpClient httpClient(GatewaySDKProperties properties) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(properties.getMaxConnections());
        connectionManager.setDefaultMaxPerRoute(properties.getMaxConnections());

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayClient gatewayClient(
            GatewaySDKProperties properties,
            ObjectMapper objectMapper,
            JwtUtils jwtUtils,
            CloseableHttpClient httpClient) {
        return new GatewayClient(properties, objectMapper, jwtUtils, httpClient);
    }
}
