package top.codelong.apigatewaycore.socket.custom.pre;

import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.core.Ordered;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;

public interface CustomPreHandler extends Ordered {
    Result handle(HttpStatement httpStatement, FullHttpRequest request);
}