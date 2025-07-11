package top.codelong.apigatewaycore.connection;

import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;

import java.util.Map;

/**
 * 基础连接接口
 */
public interface BaseConnection {
    Result send(Map<String, Object> parameter, String url, HttpStatement httpStatement);
}
