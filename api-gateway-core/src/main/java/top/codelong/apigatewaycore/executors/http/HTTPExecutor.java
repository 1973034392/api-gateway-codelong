package top.codelong.apigatewaycore.executors.http;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import top.codelong.apigatewaycore.executors.BaseExecutor;

/**
 * HTTP请求执行器接口
 */
public interface HTTPExecutor extends BaseExecutor {
    /**
     * 设置HTTP客户端
     * @param client HTTP异步客户端
     */
    void setClient(CloseableHttpAsyncClient client);
}
