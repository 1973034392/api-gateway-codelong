package top.codelong.apigatewaycore.executors;

import top.codelong.apigatewaycore.common.result.Result;
import java.util.Map;

/**
 * 执行器接口
 */
public interface BaseExecutor {
    Result execute(Map<String, Object> parameter);
}
