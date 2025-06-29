package top.codelong.apigatewaycore.socket.custom.post;

import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.core.Ordered;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;

public interface CustomPostHandler extends Ordered {
    Result handle(HttpStatement httpStatement, FullHttpRequest request);
}
