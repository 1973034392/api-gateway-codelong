package top.codelong.apigatewaycore.socket.custom.pre;

import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.core.Ordered;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;

/**
 * 自定义前置处理器接口
 */
public interface CustomPreHandler extends Ordered {
    Result<Void> handle(HttpStatement httpStatement, FullHttpRequest request);

    /**
     * 当处理器发生异常时是否快速失败
     * @return true表示发生异常时立即终止处理链，false表示继续执行下一个处理器
     */
    default boolean isFailFast() {
        return true;
    }

    /**
     * 判断处理器是否可以并行执行
     * @return true表示可以并行执行，false表示需要串行执行
     */
    default boolean canRunParallel() {
        return false;
    }
}