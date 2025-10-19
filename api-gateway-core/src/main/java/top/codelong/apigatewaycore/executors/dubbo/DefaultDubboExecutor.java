package top.codelong.apigatewaycore.executors.dubbo;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.service.GenericService;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultDubboExecutor implements DubboExecutor {
    private final Map<String, GenericService> dubboServiceMap = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Result<?>> execute(Map<String, Object> parameter, String url, HttpStatement httpStatement) {
        log.debug("开始异步执行Dubbo调用，URL: {}, 参数: {}", url, parameter);

        GenericService genericService = dubboServiceMap.computeIfAbsent(url, k -> {
            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setUrl("dubbo://" + url);
            reference.setInterface(httpStatement.getInterfaceName());
            reference.setGroup("method-group-test");
            reference.setGeneric("true");
            reference.setTimeout(3000);
            reference.setRetries(0);
            reference.setAsync(true);
            return reference.get();
        });

        String methodName = httpStatement.getMethodName();
        String[] parameterType = httpStatement.getParameterType();
        Object[] args = parameter.values().toArray();

        try {
            CompletableFuture<Object> future = genericService.$invokeAsync(methodName, parameterType, args);
            return future.<Result<?>>thenApply(Result::success)
                    .exceptionally(throwable -> {
                        log.error("Dubbo异步调用失败", throwable);
                        return Result.error("系统异常: " + throwable.getMessage());
                    });
        } catch (Exception e) {
            log.error("发起Dubbo异步调用时发生异常", e);
            return CompletableFuture.completedFuture(Result.error("系统异常: " + e.getMessage()));
        }
    }

    @Override
    public void setDubboServiceMap(Map<String, GenericService> serviceMap) {
        if (serviceMap != null) {
            this.dubboServiceMap.putAll(serviceMap);
        }
    }
}
