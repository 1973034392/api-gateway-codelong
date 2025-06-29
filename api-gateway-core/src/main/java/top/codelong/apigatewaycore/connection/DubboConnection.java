package top.codelong.apigatewaycore.connection;

import org.apache.dubbo.rpc.service.GenericService;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.executors.BaseExecutor;
import top.codelong.apigatewaycore.executors.DubboExecutor;

import java.util.Map;

/**
 * dubbo连接
 */
public class DubboConnection implements BaseConnection {
    private final BaseExecutor executor;

    public DubboConnection(String url, HttpStatement httpStatement, Map<String, GenericService> dubboServiceMap) {
        this.executor = new DubboExecutor(url, httpStatement, dubboServiceMap);
    }

    @Override
    public Result send(Map<String, Object> parameter) {
        return executor.execute(parameter);
    }
}
