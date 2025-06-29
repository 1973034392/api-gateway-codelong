package top.codelong.apigatewaycore.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import top.codelong.apigatewaycore.socket.handlers.ResultHandler;

public abstract class BaseHandler<T> extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            handle(ctx, ctx.channel(), (T) msg);
        } finally {
            // 仅在ResultHandler中手动释放
            if (this instanceof ResultHandler) {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    protected abstract void handle(ChannelHandlerContext ctx, Channel channel, T request);
}