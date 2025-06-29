package top.codelong.apigatewaycore.socket.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.socket.BaseHandler;
import top.codelong.apigatewaycore.utils.InterfaceCacheUtil;
import top.codelong.apigatewaycore.utils.JwtUtils;
import top.codelong.apigatewaycore.utils.RequestParameterUtil;
import top.codelong.apigatewaycore.utils.RequestResultUtil;

/**
 * 授权处理器
 * 负责处理接口权限验证和JWT令牌验证
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class AuthorizationHandler extends BaseHandler<FullHttpRequest> {
    @Resource
    private InterfaceCacheUtil interfaceCacheUtil;
    @Resource
    private JwtUtils jwtUtils;

    /**
     * 处理HTTP请求的授权验证
     * @param ctx ChannelHandler上下文
     * @param channel 当前Channel
     * @param request HTTP请求
     */
    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        log.debug("开始处理授权验证，URI: {}", request.uri());

        HttpStatement statement;
        try {
            // 从请求中获取URI
            String uri = RequestParameterUtil.getUrl(request);
            log.trace("解析请求URI: {}", uri);

            // 获取接口声明
            statement = interfaceCacheUtil.getStatement(uri);
            if (statement == null) {
                log.warn("接口不存在，URI: {}", uri);
                DefaultFullHttpResponse response = RequestResultUtil.parse(Result.error("暂无该接口信息"));
                channel.writeAndFlush(response);
                return;
            }

            // 检查接口是否需要认证
            if (statement.getIsAuth()) {
                log.debug("接口需要认证，URI: {}", uri);
                String token = RequestParameterUtil.getToken(request);
                log.trace("获取到的Token: {}", token);

                if (!jwtUtils.verify(token)) {
                    log.warn("Token验证失败，URI: {}", uri);
                    DefaultFullHttpResponse response = RequestResultUtil.parse(Result.error("没有权限访问该接口!"));
                    channel.writeAndFlush(response);
                    return;
                }
                log.debug("Token验证成功，URI: {}", uri);
            }
        } catch (Exception e) {
            log.error("授权验证处理异常", e);
            DefaultFullHttpResponse response = RequestResultUtil.parse(Result.error("接口调用失败: " + e.getMessage()));
            channel.writeAndFlush(response);
            return;
        }

        // 将接口声明存入Channel属性
        channel.attr(AttributeKey.valueOf("HttpStatement")).set(statement);
        log.debug("授权验证通过，继续处理请求，URI: {}", request.uri());

        // 传递给下一个处理器
        ctx.fireChannelRead(request);
    }
}