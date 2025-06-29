package top.codelong.apigatewaycore.socket.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.GatewayServer;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.config.GlobalConfiguration;
import top.codelong.apigatewaycore.connection.BaseConnection;
import top.codelong.apigatewaycore.connection.DubboConnection;
import top.codelong.apigatewaycore.connection.HTTPConnection;
import top.codelong.apigatewaycore.socket.BaseHandler;
import top.codelong.apigatewaycore.utils.RequestParameterUtil;
import top.codelong.apigatewaycore.utils.RequestResultUtil;

import java.util.Map;

@Component
@ChannelHandler.Sharable
public class ExecutorHandler extends BaseHandler<FullHttpRequest> {
    @Resource
    private GlobalConfiguration config;
    @Resource
    private GatewayServer gatewayServer;

    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        HttpStatement httpStatement = (HttpStatement) channel.attr(AttributeKey.valueOf("HttpStatement")).get();
        Map<String, Object> parameters = RequestParameterUtil.getParameters(request);
        BaseConnection connection;
        String url = RequestParameterUtil.getUrl(request);
        String serverAddr = gatewayServer.getOne();
        if (httpStatement.getIsHttp()) {
            url = "http://" + serverAddr + url;
            connection = new HTTPConnection(url, httpStatement, config.getHttpClient());
        } else {
            url = serverAddr.split(":")[0] + ":20880";
            connection = new DubboConnection(url, httpStatement, config.getDubboServiceMap());
        }

        try {
            Result data = connection.send(parameters);
            channel.attr(AttributeKey.valueOf("data")).set(data);
        } catch (Exception e) {
            DefaultFullHttpResponse response = RequestResultUtil.parse(Result.error("服务调用失败"));
            channel.writeAndFlush(response);
            return;
        }
        ctx.fireChannelRead(request);
    }
}