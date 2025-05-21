package top.codelong.apigatewaycenter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nginx")
public class NginxConfig {
    private String address;
    private String config;
}
