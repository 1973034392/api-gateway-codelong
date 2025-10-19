package top.codelong.apigatewaycore.socket.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.socket.BaseHandler;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ChannelHandler.Sharable
public class ResultHandler extends BaseHandler<FullHttpRequest> {
    private static final AttributeKey<HttpStatement> HTTP_STATEMENT_KEY = AttributeKey.valueOf("HttpStatement");

    // 使用简单的内存缓存替代 Caffeine
    private static final ConcurrentHashMap<String, byte[]> responseCache = new ConcurrentHashMap<>();

    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        try {
            HttpStatement httpStatement = channel.attr(HTTP_STATEMENT_KEY).get();
            if (httpStatement == null) {
                // 释放请求资源
                request.release();
                sendError(channel, "系统处理异常");
                return;
            }

            String cacheKey = generateCacheKey(request, httpStatement);
            byte[] cachedResponse = responseCache.get(cacheKey);

            if (cachedResponse != null) {
                sendCachedResponse(channel, cachedResponse, request);
                // 释放请求资源
                request.release();
                return;
            }

            // 构建成功响应
            Result<?> result = Result.success();
            byte[] responseContent = result.toString().getBytes();

            responseCache.put(cacheKey, responseContent);
            sendResponse(channel, responseContent, request);
            // 释放请求资源
            request.release();

        } catch (Exception e) {
            // 释放请求资源
            request.release();
            log.error("响应处理异常", e);
            sendError(channel, "系统处理异常");
        }
    }

    private void sendResponse(Channel channel, byte[] content, FullHttpRequest request) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                request.protocolVersion(),
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content)
        );

        response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "application/json")
                .set(HttpHeaderNames.CONTENT_LENGTH, content.length)
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        channel.writeAndFlush(response);
    }

    private void sendCachedResponse(Channel channel, byte[] content, FullHttpRequest request) {
        sendResponse(channel, content, request);
    }

    private String generateCacheKey(FullHttpRequest request, HttpStatement statement) {
        return statement.getInterfaceName() + ":" + request.uri();
    }

    private void sendError(Channel channel, String message) {
        Result<?> errorResult = Result.error(message);
        byte[] content = errorResult.toString().getBytes();
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content)
        );
        response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "application/json")
                .set(HttpHeaderNames.CONTENT_LENGTH, content.length)
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        channel.writeAndFlush(response);
    }
}
