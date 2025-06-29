package top.codelong.apigatewaycore.executors;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;

import java.util.Map;

@Slf4j
public class DubboExecutor implements BaseExecutor {
    private final String url;
    private final HttpStatement httpStatement;
    private final Map<String, GenericService> dubboServiceMap;

    public DubboExecutor(String url, HttpStatement httpStatement, Map<String, GenericService> dubboServiceMap) {
        this.url = url;
        this.httpStatement = httpStatement;
        this.dubboServiceMap = dubboServiceMap;
    }

    @Override
    public Result execute(Map<String, Object> parameter) {
        GenericService genericService = dubboServiceMap.get(url);
        if (genericService == null) {
            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setUrl("dubbo://" + url);
            reference.setInterface(httpStatement.getInterfaceName());
            reference.setGroup("method-group-test");
            reference.setGeneric("true");
            reference.setTimeout(3000);
            reference.setRetries(0);

            genericService = reference.get();
        }

        String methodName = httpStatement.getMethodName();
        String[] parameterType = httpStatement.getParameterType();
        try {
            // TODO 动态参数处理
            Object[] args = parameter.values().toArray();
            Object o = genericService.$invoke(
                    methodName,
                    parameterType,
                    args
            );
            return Result.success(o);
        } catch (RpcException e) {
            log.error("Dubbo调用失败: {}", e.getMessage());
            return Result.error("Dubbo调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("系统异常: {}", e.getMessage());
            return Result.error("系统异常: " + e.getMessage());
        }
    }
}
