package top.codelong.apigatewaycore.connection;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.executors.BaseExecutor;
import top.codelong.apigatewaycore.executors.http.HTTPExecutorSpiFinder;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP连接
 */
public class HTTPConnection implements BaseConnection {
    private final BaseExecutor executor;
    private final String baseUrl;

    public HTTPConnection(CloseableHttpAsyncClient CloseableHttpClient, String baseUrl) {
        this.executor = HTTPExecutorSpiFinder.getInstance(CloseableHttpClient);
        this.baseUrl = baseUrl;
    }

    @Override
    public CompletableFuture<Result<?>> send(Map<String, Object> parameter, HttpStatement httpStatement) {
        return executor.execute(parameter, baseUrl, httpStatement);
    }
}