package top.codelong.apigatewaycore.socket.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.socket.BaseHandler;
import top.codelong.apigatewaycore.utils.InterfaceCacheUtil;
import top.codelong.apigatewaycore.utils.JwtUtils;
import top.codelong.apigatewaycore.utils.RequestParameterUtil;
import top.codelong.apigatewaycore.utils.RequestResultUtil;

@Component
public class AuthorizationHandler extends BaseHandler<FullHttpRequest> {
    @Resource
    private InterfaceCacheUtil interfaceCacheUtil;
    @Resource
    private JwtUtils jwtUtils;

    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        HttpStatement statement = null;
        try {
            String uri = RequestParameterUtil.getUri(request);
            statement = interfaceCacheUtil.getStatement(uri);

            if (statement == null) {
                DefaultFullHttpResponse response = RequestResultUtil.parse(Result.error("暂无该接口信息"));
                channel.writeAndFlush(response);
            }

            if (statement != null && statement.getIsAuth()) {
                String token = RequestParameterUtil.getToken(request);
                if (!jwtUtils.verify(token)) {
                    DefaultFullHttpResponse response = RequestResultUtil.parse(Result.error("没有权限访问该接口!"));
                    channel.writeAndFlush(response);
                }
            }
        } catch (Exception e) {
            DefaultFullHttpResponse response = RequestResultUtil.parse(Result.error("接口调用失败: " + e.getMessage()));
            channel.writeAndFlush(response);
        }

        channel.attr(AttributeKey.valueOf("HttpStatement")).set(statement);
        request.retain();
        ctx.fireChannelRead(request);
    }
}
