package top.codelong.findsdk.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.codelong.findsdk.service.GatewayRegisterService;

/**
 * @author CodeLong
 * @description 网关SDK配置服务
 */
@Configuration
@EnableConfigurationProperties(GatewayServerConfig.class)
@ConditionalOnProperty(
        prefix = "api-gateway-sdk",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class GatewaySDKAutoConfig {

    @Bean
    public GatewayRegisterService gatewayRegisterService() {
        return new GatewayRegisterService();
    }
}
