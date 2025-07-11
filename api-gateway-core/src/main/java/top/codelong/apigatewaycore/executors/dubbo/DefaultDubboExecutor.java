package top.codelong.apigatewaycore.executors.dubbo;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.executors.http.DefaultHTTPExecutor;

import java.util.Map;

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
     * 执行Dubbo泛化调用
     *
     * @param parameter 请求参数
     * @return 调用结果
     */
    @Override
    public Result execute(Map<String, Object> parameter, String url, HttpStatement httpStatement) {
        log.debug("开始执行Dubbo调用，URL: {}, 参数: {}", url, parameter);

        // 从缓存获取或创建GenericService
        GenericService genericService = dubboServiceMap.computeIfAbsent(url, k -> {
            log.debug("创建新的Dubbo引用配置，URL: {}", url);
            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setUrl("dubbo://" + url);
            reference.setInterface(httpStatement.getInterfaceName());
            reference.setGroup("method-group-test");
            reference.setGeneric("true");
            reference.setTimeout(3000);
            reference.setRetries(0);
            return reference.get();
        });

        String methodName = httpStatement.getMethodName();
        String[] parameterType = httpStatement.getParameterType();

        try {
            log.debug("准备调用Dubbo方法，方法名: {}, 参数类型: {}", methodName, parameterType);

            // 转换参数
            Object[] args = parameter.values().toArray();
            log.trace("方法调用参数: {}", args);

            // 执行泛化调用
            Object result = genericService.$invoke(methodName, parameterType, args);
            log.debug("Dubbo调用成功，方法名: {}", methodName);

            return Result.success(result);
        } catch (RpcException e) {
            log.error("Dubbo调用失败，方法名: {}, 错误: {}", methodName, e.getMessage(), e);
            return Result.error("Dubbo调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("系统异常，方法名: {}, 错误: {}", methodName, e.getMessage(), e);
            return Result.error("系统异常: " + e.getMessage());
        }
    }
}