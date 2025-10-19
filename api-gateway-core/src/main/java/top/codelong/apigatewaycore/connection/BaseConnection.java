package top.codelong.apigatewaycore.connection;

import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 基础连接接口
 */
public interface BaseConnection {
    /**
     * 发送请求
     *
     * @param parameter     请求参数
     * @param httpStatement HTTP语句
     * @return 异步结果
     */
    CompletableFuture<Result<?>> send(Map<String, Object> parameter, HttpStatement httpStatement);

    /**
     * 关闭连接资源
     *
     * @throws Exception 关闭过程中可能发生的异常
     */
    default void close() throws Exception {
        // 默认空实现
    }
}
