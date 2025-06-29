package top.codelong.apigatewaycore.socket.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.socket.BaseHandler;
import top.codelong.apigatewaycore.socket.custom.post.CustomPostHandler;
import top.codelong.apigatewaycore.socket.custom.pre.CustomPreHandler;
import top.codelong.apigatewaycore.utils.RequestResultUtil;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ChannelHandler.Sharable
public class PostExecutorHandler extends BaseHandler<FullHttpRequest> {
    private final List<CustomPostHandler> handlers;

    @Autowired
    public PostExecutorHandler(List<CustomPostHandler> customPostHandlers) {
        this.handlers = customPostHandlers.stream()
                .sorted(Comparator.comparingInt(Ordered::getOrder))
                .collect(Collectors.toList());
    }

    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        HttpStatement httpStatement = (HttpStatement) channel.attr(AttributeKey.valueOf("HttpStatement")).get();
        for (CustomPostHandler handler : handlers) {
            Result result = null;
            try {
                result = handler.handle(httpStatement, request);
            } catch (Exception ignore) {
            }
            if (result != null) {
                DefaultFullHttpResponse response = RequestResultUtil.parse(result);
                channel.writeAndFlush(response);
                return;
            }
        }
        ctx.fireChannelRead(request);
    }
}
