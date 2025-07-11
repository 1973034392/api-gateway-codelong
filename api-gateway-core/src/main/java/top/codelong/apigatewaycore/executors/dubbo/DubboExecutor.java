package top.codelong.apigatewaycore.executors.dubbo;

import org.apache.dubbo.rpc.service.GenericService;
import top.codelong.apigatewaycore.executors.BaseExecutor;

import java.util.Map;

public interface DubboExecutor extends BaseExecutor {
    void setDubboServiceMap(Map<String, GenericService> dubboServiceMap);
}
