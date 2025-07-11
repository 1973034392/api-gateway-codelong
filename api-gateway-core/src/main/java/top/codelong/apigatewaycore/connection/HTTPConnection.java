package top.codelong.apigatewaycore.connection;

import org.apache.http.impl.client.CloseableHttpClient;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.executors.BaseExecutor;
import top.codelong.apigatewaycore.executors.http.HTTPExecutorSpiFinder;

import java.util.Map;

/**
 * HTTP连接
 */
public class HTTPConnection implements BaseConnection {
    private final BaseExecutor executor;

    public HTTPConnection(CloseableHttpClient CloseableHttpClient) {
        this.executor = HTTPExecutorSpiFinder.getInstance(CloseableHttpClient);
    }

    @Override
    public Result send(Map<String, Object> parameter, String url, HttpStatement httpStatement) {
        return executor.execute(parameter, url, httpStatement);
    }
}
