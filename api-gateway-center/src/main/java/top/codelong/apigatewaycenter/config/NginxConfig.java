package top.codelong.apigatewaycenter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nginx.config.remote")
public class NginxConfig {
    private String host;
    private int port = 22;
    private String username;
    private String password;
    private String configPath;
    private String reloadCommand;
}
