package top.codelong.apigatewaycore.socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import jakarta.annotation.Resource;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import top.codelong.apigatewaycore.socket.handlers.*;

@Component
public class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {
    @Resource
    private AuthorizationHandler authorizationHandler;

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline line = channel.pipeline();
        line.addLast(new HttpRequestDecoder());
        line.addLast(new HttpResponseEncoder());
        line.addLast(new HttpObjectAggregator(1024 * 1024));
        line.addLast(authorizationHandler);
//        line.addLast(new PreExecutorHandler());
//        line.addLast(new ExecutorHandler());
//        line.addLast(new PostExecutorHandler());
//        line.addLast(new ResultHandler());
    }
}
