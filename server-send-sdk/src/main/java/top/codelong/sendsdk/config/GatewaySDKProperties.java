package top.codelong.sendsdk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api.gateway.sdk")
public class GatewaySDKProperties {
    private String baseUrl;
    private String safeKey;
    private String safeSecret;
    private int connectTimeout = 5000;
    private int socketTimeout = 10000;
    private int maxConnections = 100;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getSafeKey() {
        return safeKey;
    }

    public void setSafeKey(String safeKey) {
        this.safeKey = safeKey;
    }

    public String getSafeSecret() {
        return safeSecret;
    }

    public void setSafeSecret(String safeSecret) {
        this.safeSecret = safeSecret;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
}