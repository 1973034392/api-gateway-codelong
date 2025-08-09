package top.codelong.apigatewaycore.executors.http;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import top.codelong.apigatewaycore.executors.BaseExecutor;

public interface HTTPExecutor extends BaseExecutor {
    void setClient(CloseableHttpAsyncClient client);
}
