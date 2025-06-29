package top.codelong.apigatewaycore.connection;

import top.codelong.apigatewaycore.common.result.Result;

import java.util.Map;

public interface BaseConnection {
    Result send(Map<String, Object> parameter);
}
