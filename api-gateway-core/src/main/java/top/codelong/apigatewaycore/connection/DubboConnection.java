package top.codelong.apigatewaycore.connection;

import org.apache.dubbo.rpc.service.GenericService;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.executors.BaseExecutor;
import top.codelong.apigatewaycore.executors.dubbo.DubboExecutorSpiFinder;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * dubbo连接
 */
public class DubboConnection implements BaseConnection {
    private final BaseExecutor executor;
    private final String url;

    public DubboConnection(Map<String, GenericService> dubboServiceMap, String url) {
        this.executor = DubboExecutorSpiFinder.getInstance(dubboServiceMap);
        this.url = url;
    }

    @Override
    public CompletableFuture<Result<?>> send(Map<String, Object> parameter, HttpStatement httpStatement) {
        return executor.execute(parameter, this.url, httpStatement);
    }
}