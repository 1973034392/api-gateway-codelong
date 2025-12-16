package top.codelong.apigatewaycore.socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.socket.handlers.*;

/**
 * 服务端初始化器
 * 处理链顺序：
 * 1. HTTP 编解码器
 * 2. 鉴权处理器 (AuthorizationHandler)
 * 3. 限流处理器 (RateLimitHandler) - 新增
 * 4. 前置处理器 (PreExecutorHandler)
 * 5. 执行器 (ExecutorHandler)
 * 6. 后置处理器 (PostExecutorHandler)
 * 7. 结果处理器 (ResultHandler)
 */
@Component
public class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {
    @Resource
    private AuthorizationHandler authorizationHandler;
    @Resource
    private RateLimitHandler rateLimitHandler;
    @Resource
    private PreExecutorHandler preExecutorHandler;
    @Resource
    private ExecutorHandler executorHandler;
    @Resource
    private PostExecutorHandler postExecutorHandler;
    @Resource
    private ResultHandler resultHandler;

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline line = channel.pipeline();
        line.addLast(new HttpRequestDecoder());
        line.addLast(new HttpResponseEncoder());
        line.addLast(new HttpObjectAggregator(1024 * 1024 * 10));
        line.addLast(authorizationHandler);
        line.addLast(rateLimitHandler);
        line.addLast(preExecutorHandler);
        line.addLast(executorHandler);
        line.addLast(postExecutorHandler);
        line.addLast(resultHandler);
    }
}