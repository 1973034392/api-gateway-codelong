package top.codelong.apigatewaycore.connection;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.http.impl.client.CloseableHttpClient;
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

    public HTTPConnection(CloseableHttpAsyncClient CloseableHttpClient) {
        this.executor = HTTPExecutorSpiFinder.getInstance(CloseableHttpClient);
    }

    @Override
    public CompletableFuture<Result> send(Map<String, Object> parameter, String url, HttpStatement httpStatement) {
        return executor.execute(parameter, url, httpStatement);
    }
}
