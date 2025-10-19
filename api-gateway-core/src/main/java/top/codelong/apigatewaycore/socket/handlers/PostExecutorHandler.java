package top.codelong.apigatewaycore.socket.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.socket.BaseHandler;
import top.codelong.apigatewaycore.socket.custom.post.CustomPostHandler;
import top.codelong.apigatewaycore.utils.RequestResultUtil;

import java.util.List;
import java.util.concurrent.*;

/**
 * 后置执行处理器
 * 负责执行所有自定义的后置处理器链
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class PostExecutorHandler extends BaseHandler<FullHttpRequest> {
    private static final AttributeKey<HttpStatement> HTTP_STATEMENT_KEY = AttributeKey.valueOf("HttpStatement");
    private static final int HANDLER_TIMEOUT_MS = 2000; // 后置处理器超时时间2秒

    private final ExecutorService executorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(500),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Resource
    private List<CustomPostHandler> postHandlers;

    /**
     * 处理HTTP请求的后置处理
     * @param ctx ChannelHandler上下文
     * @param channel 当前Channel
     * @param request HTTP请求
     */
    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        HttpStatement httpStatement = channel.attr(HTTP_STATEMENT_KEY).get();
        if (httpStatement == null) {
            sendError(channel, "系统处理异常");
            return;
        }

        // 并行执行所有后置处理器
        CompletableFuture.allOf(
                postHandlers.stream()
                        .map(handler -> executeHandler(handler, httpStatement, request))
                        .toArray(CompletableFuture[]::new)
        ).whenComplete((result, throwable) -> {
            // 释放请求资源
            request.release();
            if (throwable != null) {
                log.error("后置处理器执行异常", throwable);
                sendError(channel, "处理器执行异常");
                return;
            }
            ctx.fireChannelRead(request);
        });
    }

    private CompletableFuture<Void> executeHandler(CustomPostHandler handler,
            HttpStatement statement, FullHttpRequest request) {
        return CompletableFuture.runAsync(() -> {
            try {
                handler.handle(statement, request);
            } catch (Exception e) {
                log.error("后置处理器[{}]执行异常", handler.getClass().getSimpleName(), e);
                // 后置处理器异常不影响主流程
            }
        }, executorService).orTimeout(HANDLER_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .exceptionally(throwable -> {
            log.error("后置处理器执行超时或异常", throwable);
            return null; // 后置处理器异常不影响主流程
        });
    }

    private void sendError(Channel channel, String message) {
        channel.writeAndFlush(RequestResultUtil.parse(Result.error(message)));
    }
}