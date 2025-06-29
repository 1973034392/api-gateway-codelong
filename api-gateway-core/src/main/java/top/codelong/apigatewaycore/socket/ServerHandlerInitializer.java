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
 */
@Component
public class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {
    @Resource
    private AuthorizationHandler authorizationHandler;
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
        line.addLast(preExecutorHandler);
        line.addLast(executorHandler);
        line.addLast(postExecutorHandler);
        line.addLast(resultHandler);
    }
}