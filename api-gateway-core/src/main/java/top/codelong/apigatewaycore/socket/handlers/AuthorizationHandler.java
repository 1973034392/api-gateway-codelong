package top.codelong.apigatewaycore.socket.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 授权处理器
 * 负责处理接口权限验证和JWT令牌验证
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class AuthorizationHandler extends BaseHandler<FullHttpRequest> {
    private static final AttributeKey<HttpStatement> HTTP_STATEMENT_KEY = AttributeKey.valueOf("HttpStatement");

    // 用本地缓存来存储已验证的token
    private static final Cache<String, Boolean> tokenCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @Resource
    private InterfaceCacheUtil interfaceCacheUtil;
    @Resource
    private JwtUtils jwtUtils;

    /**
     * 处理HTTP请求的授权验证
     */
    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        log.debug("开始处理授权验证，URI: {}", request.uri());

        CompletableFuture.supplyAsync(() -> {
            try {
                String uri = RequestParameterUtil.getUrl(request);
                log.trace("解析请求URI: {}", uri);

                HttpStatement statement = interfaceCacheUtil.getStatement(uri);
                if (statement == null) {
                    throw new IllegalArgumentException("接口不存在: " + uri);
                }

                if (statement.getIsAuth()) {
                    String token = RequestParameterUtil.getToken(request);
                    // 首先检查本地缓存
                    Boolean isValid = tokenCache.getIfPresent(token);
                    if (isValid == null) {
                        // 缓存中没有，执行验证
                        isValid = jwtUtils.verify(token);
                        if (isValid) {
                            tokenCache.put(token, true);
                        } else {
                            throw new IllegalArgumentException("认证失败");
                        }
                    }
                }

                return statement;
            } catch (Exception e) {
                log.error("授权验证处理异常: {}", e.getMessage(), e);
                throw e;
            }
        }).whenComplete((statement, throwable) -> {
            if (throwable != null) {
                // 释放请求资源
                request.release();
                sendError(channel, throwable.getMessage());
                return;
            }

            channel.attr(HTTP_STATEMENT_KEY).set(statement);
            ctx.fireChannelRead(request);
        });
    }

    private void sendError(Channel channel, String message) {
        DefaultFullHttpResponse response = RequestResultUtil.parse(Result.error(message));
        channel.writeAndFlush(response);
    }
}