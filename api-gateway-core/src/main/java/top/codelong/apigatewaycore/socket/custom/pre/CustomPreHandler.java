package top.codelong.apigatewaycore.socket.custom.pre;

import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.core.Ordered;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;

/**
 * 自定义前置处理器接口
 */
public interface CustomPreHandler extends Ordered {
    Result handle(HttpStatement httpStatement, FullHttpRequest request);
}