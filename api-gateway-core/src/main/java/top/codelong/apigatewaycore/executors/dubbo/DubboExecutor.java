package top.codelong.apigatewaycore.executors.dubbo;

import org.apache.dubbo.rpc.service.GenericService;
import top.codelong.apigatewaycore.executors.BaseExecutor;

import java.util.Map;

public interface DubboExecutor extends BaseExecutor {
    /**
     * 设置 Dubbo 服务映射
     * @param dubboServiceMap Dubbo 服务映射
     */
    void setDubboServiceMap(Map<String, GenericService> dubboServiceMap);
}
