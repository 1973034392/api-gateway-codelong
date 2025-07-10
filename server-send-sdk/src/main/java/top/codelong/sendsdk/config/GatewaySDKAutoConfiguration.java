package top.codelong.sendsdk.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.codelong.sendsdk.client.GatewayClient;
import top.codelong.sendsdk.client.HttpClientFactory;
import top.codelong.sendsdk.utils.JwtUtils;


/**
 * 网关SDK自动配置类
 */
@Configuration
@ConditionalOnClass(name = "org.apache.http.client.HttpClient")
@EnableConfigurationProperties(GatewaySDKProperties.class)
@ConditionalOnProperty(prefix = "api.gateway.sdk", name = "base-url")
public class GatewaySDKAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils() {
        return new JwtUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    // 添加HttpClient Bean定义
    @Bean
    @ConditionalOnMissingBean
    public CloseableHttpClient httpClient(GatewaySDKProperties properties) {
        return HttpClientFactory.createHttpClient(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayClient gatewayClient(
            GatewaySDKProperties properties,
            ObjectMapper objectMapper,
            JwtUtils jwtUtils,
            CloseableHttpClient httpClient) {

        return new GatewayClient(
                properties,
                objectMapper,
                jwtUtils,
                httpClient // 传入httpClient实例
        );
    }
}