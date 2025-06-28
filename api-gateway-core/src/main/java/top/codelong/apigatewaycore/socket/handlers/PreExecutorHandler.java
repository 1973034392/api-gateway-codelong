package top.codelong.apigatewaycore.socket.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import top.codelong.apigatewaycore.socket.BaseHandler;

public class PreExecutorHandler extends BaseHandler<FullHttpRequest> {
    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        ctx.fireChannelRead(request);
    }
}
