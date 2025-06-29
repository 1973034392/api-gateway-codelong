package top.codelong.apigatewaycore.socket.custom.post;

import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;

/**
 * 测试自定义后置处理器
 */
@Component
@Slf4j
@Order(1)
public class TestPostHandler implements CustomPostHandler{
    @Override
    public Result handle(HttpStatement httpStatement, FullHttpRequest request) {
        log.info("======TestPostHandler======");
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
