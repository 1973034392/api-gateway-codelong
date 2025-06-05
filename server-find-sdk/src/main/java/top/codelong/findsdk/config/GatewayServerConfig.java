package top.codelong.findsdk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway-server")
public class GatewayServerConfig {
    private String serverName;
    private String centerAddr;
    private String safeKey;
    private String safeSecret;

    public String getServerName() {
        return serverName;
    }

    public String getCenterAddr() {
        return centerAddr;
    }

    public String getSafeKey() {
        return safeKey;
    }

    public String getSafeSecret() {
        return safeSecret;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setCenterAddr(String centerAddr) {
        this.centerAddr = centerAddr;
    }

    public void setSafeKey(String safeKey) {
        this.safeKey = safeKey;
    }

    public void setSafeSecret(String safeSecret) {
        this.safeSecret = safeSecret;
    }
}