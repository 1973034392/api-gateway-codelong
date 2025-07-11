package top.codelong.apigatewaycore.executors.http;

import org.apache.http.impl.client.CloseableHttpClient;
import top.codelong.apigatewaycore.executors.BaseExecutor;

public interface HTTPExecutor extends BaseExecutor {
    void setClient(CloseableHttpClient client);
}
