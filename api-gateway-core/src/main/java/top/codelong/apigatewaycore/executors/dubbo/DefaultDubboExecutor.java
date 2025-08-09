package top.codelong.apigatewaycore.executors.dubbo;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.service.GenericService;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Dubbo服务执行器
 * 负责执行Dubbo泛化调用
 */
@Slf4j
public class DefaultDubboExecutor implements DubboExecutor {
    private volatile Map<String, GenericService> dubboServiceMap;

    public void setDubboServiceMap(Map<String, GenericService> dubboServiceMap) {
        if (this.dubboServiceMap == null) {
            synchronized (DefaultDubboExecutor.class) {
                if (this.dubboServiceMap == null) {
                    try {
                        this.dubboServiceMap = dubboServiceMap;
                    } catch (Exception ignore) {
                    }
                }
            }
        }
    }

    /**
     * 异步执行Dubbo泛化调用
     *
     * @param parameter 请求参数
     * @return 包含调用结果的CompletableFuture
     */
    @Override
    public CompletableFuture<Result> execute(Map<String, Object> parameter, String url, HttpStatement httpStatement) {
        log.debug("开始异步执行Dubbo调用，URL: {}, 参数: {}", url, parameter);

        GenericService genericService = dubboServiceMap.computeIfAbsent(url, k -> {
            // ... (原有创建ReferenceConfig的代码不变)
            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setUrl("dubbo://" + url);
            reference.setInterface(httpStatement.getInterfaceName());
            reference.setGroup("method-group-test");
            reference.setGeneric("true");
            reference.setTimeout(3000);
            reference.setRetries(0);
            // ******** 开启异步调用 ********
            reference.setAsync(true);
            // ***************************
            return reference.get();
        });

        String methodName = httpStatement.getMethodName();
        String[] parameterType = httpStatement.getParameterType();
        Object[] args = parameter.values().toArray();

        try {
            // ******** Dubbo 3.x 推荐的异步方式 ********
            CompletableFuture<Object> future = genericService.$invokeAsync(methodName, parameterType, args);
            return future.thenApply(Result::success);
        } catch (Exception e) {
            log.error("发起Dubbo异步调用时发生异常", e);
            return CompletableFuture.completedFuture(Result.error("系统异常: " + e.getMessage()));
        }
    }
}