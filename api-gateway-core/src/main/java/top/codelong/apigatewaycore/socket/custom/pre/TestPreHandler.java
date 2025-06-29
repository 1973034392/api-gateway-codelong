package top.codelong.apigatewaycore.socket.custom.pre;

import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.socket.custom.post.CustomPostHandler;

/**
 * 测试自定义前置处理器
 */
@Component
@Slf4j
@Order(1)
public class TestPreHandler implements CustomPreHandler {
    @Override
    public Result handle(HttpStatement httpStatement, FullHttpRequest request) {
        log.info("======TestPreHandler======");
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
