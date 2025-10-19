package top.codelong.apigatewaycore.socket.custom.post;

import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.core.Ordered;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;

/**
 * 自定义后置处理器接口
 */
public interface CustomPostHandler extends Ordered {
    /**
     * 处理请求
     * @param httpStatement HTTP语句
     * @param request HTTP请求
     * @return 处理结果
     */
    Result handle(HttpStatement httpStatement, FullHttpRequest request);

    /**
     * 当处理器发生异常时是否快速失败
     * @return true表示发生异常时立即终止处理链，false表示继续执行下一个处理器
     */
    default boolean isFailFast() {
        return true;
    }
}
