package top.codelong.apigatewaycore.socket.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.socket.BaseHandler;
import top.codelong.apigatewaycore.utils.RequestResultUtil;

/**
 * 结果包装处理器
 */
@Component
@ChannelHandler.Sharable
public class ResultHandler extends BaseHandler<FullHttpRequest> {
    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        Result result = Result.success(channel.attr(AttributeKey.valueOf("data")).get());
        channel.writeAndFlush(RequestResultUtil.parse(result));
    }
}
