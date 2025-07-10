package top.codelong.sendsdk.client;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.codelong.sendsdk.config.GatewaySDKProperties;

import java.util.concurrent.TimeUnit;

public class HttpClientFactory {
    private static final Logger log = LoggerFactory.getLogger(HttpClientFactory.class);

    public static CloseableHttpClient createHttpClient(GatewaySDKProperties properties) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(properties.getMaxConnections());
        cm.setDefaultMaxPerRoute(properties.getMaxConnections() / 2);

        cm.setValidateAfterInactivity(30_000); // 30秒后验证连接

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(properties.getConnectTimeout())
                .setSocketTimeout(properties.getSocketTimeout())
                .build();

        log.info("创建HTTP客户端: 最大连接数={}, 连接超时={}ms, 读取超时={}ms",
                properties.getMaxConnections(),
                properties.getConnectTimeout(),
                properties.getSocketTimeout());

        return HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(config)
                // 启用空闲连接清理
                .evictIdleConnections(60L, TimeUnit.SECONDS)
                .build();
    }
}