package top.codelong.apigatewaycore.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public abstract class BaseHandler<T> extends SimpleChannelInboundHandler<T> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, T t) {
        handle(channelHandlerContext, channelHandlerContext.channel(), t);
    }

    protected abstract void handle(ChannelHandlerContext ctx, Channel channel, T request);
}