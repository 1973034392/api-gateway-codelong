package top.codelong.apigatewaycore.connection;

import org.apache.http.impl.client.CloseableHttpClient;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.executors.BaseExecutor;
import top.codelong.apigatewaycore.executors.HTTPExecutor;

import java.util.Map;

/**
 * HTTP连接
 */
public class HTTPConnection implements BaseConnection {
    private final BaseExecutor executor;

    public HTTPConnection(String url, HttpStatement httpStatement, CloseableHttpClient CloseableHttpClient) {
        this.executor = new HTTPExecutor(url, httpStatement, CloseableHttpClient);
    }

    @Override
    public Result send(Map<String, Object> parameter) {
        return executor.execute(parameter);
    }
}
