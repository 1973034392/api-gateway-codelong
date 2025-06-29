package top.codelong.apigatewaycore.socket.custom.post;

import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.core.Ordered;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;

/**
 * 自定义后置处理器接口
 */
public interface CustomPostHandler extends Ordered {
    Result handle(HttpStatement httpStatement, FullHttpRequest request);
}
