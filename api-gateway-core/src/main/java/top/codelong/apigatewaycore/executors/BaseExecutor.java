package top.codelong.apigatewaycore.executors;

import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 基础执行器接口
 */
public interface BaseExecutor {
    /**
     * 执行请求
     * @param parameter 请求参数
     * @param url 目标URL
     * @param httpStatement HTTP声明
     * @return 异步执行结果
     */
    CompletableFuture<Result<?>> execute(Map<String, Object> parameter, String url, HttpStatement httpStatement);
}
