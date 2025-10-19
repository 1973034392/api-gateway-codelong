package top.codelong.apigatewaycore.socket.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.socket.BaseHandler;
import top.codelong.apigatewaycore.socket.custom.pre.CustomPreHandler;
import top.codelong.apigatewaycore.utils.RequestResultUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@ChannelHandler.Sharable
public class PreExecutorHandler extends BaseHandler<FullHttpRequest> {
    private static final AttributeKey<HttpStatement> HTTP_STATEMENT_KEY = AttributeKey.valueOf("HttpStatement");
    private static final int HANDLER_TIMEOUT_MS = 3000;

    private static final Cache<String, Result<Void>> handlerResultCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    private final List<CustomPreHandler> serialHandlers;
    private final List<CustomPreHandler> parallelHandlers;

    private final ExecutorService executorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(500),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Autowired
    public PreExecutorHandler(List<CustomPreHandler> customPreHandlers) {
        // 根据处理器特性分类
        this.serialHandlers = customPreHandlers.stream()
                .filter(handler -> !handler.canRunParallel())
                .sorted(Comparator.comparingInt(Ordered::getOrder))
                .collect(Collectors.toList());

        this.parallelHandlers = customPreHandlers.stream()
                .filter(CustomPreHandler::canRunParallel)
                .collect(Collectors.toList());

        log.info("初始化PreExecutorHandler，串行处理器：{}个，并行处理器：{}个",
                serialHandlers.size(), parallelHandlers.size());
    }

    /**
     * 处理HTTP请求，按顺序执行所有前置处理器
     * @param ctx Netty上下文
     * @param channel 通信通道
     * @param request HTTP请求
     */
    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        HttpStatement httpStatement = channel.attr(HTTP_STATEMENT_KEY).get();
        if (httpStatement == null) {
            sendError(channel, "系统处理异常");
            return;
        }

        String cacheKey = generateCacheKey(request, httpStatement);
        Result<Void> cachedResult = handlerResultCache.getIfPresent(cacheKey);
        if (cachedResult != null) {
            if (!cachedResult.getCode().equals(200)) {
                sendError(channel, cachedResult.getMsg());
                return;
            }
            ctx.fireChannelRead(request);
            return;
        }

        // 并行执行处理器
        CompletableFuture<Void> parallelTasks = CompletableFuture.allOf(
                parallelHandlers.stream()
                        .map(handler -> executeHandler(handler, httpStatement, request))
                        .toArray(CompletableFuture[]::new)
        );

        // 串行执行处理器
        CompletableFuture<Result<Void>> serialTasks = parallelTasks.thenCompose(unused -> {
            CompletableFuture<Result<Void>> future = CompletableFuture.completedFuture(null);
            for (CustomPreHandler handler : serialHandlers) {
                future = future.thenCompose(result -> {
                    if (result != null && !result.getCode().equals(200)) {
                        return CompletableFuture.completedFuture(result);
                    }
                    return executeHandler(handler, httpStatement, request);
                });
            }
            return future;
        });

        // 处理最终结果
        serialTasks.whenComplete((finalResult, throwable) -> {
            if (throwable != null) {
                // 释放请求资源
                request.release();
                log.error("处理器执行异常", throwable);
                sendError(channel, "处理器执行异常");
                return;
            }

            if (finalResult != null) {
                handlerResultCache.put(cacheKey, finalResult);
                if (!finalResult.getCode().equals(200)) {
                    // 释放请求资源
                    request.release();
                    sendError(channel, finalResult.getMsg());
                    return;
                }
            }

            ctx.fireChannelRead(request);
        });
    }

    private CompletableFuture<Result<Void>> executeHandler(CustomPreHandler handler,
            HttpStatement statement, FullHttpRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return handler.handle(statement, request);
            } catch (Exception e) {
                log.error("处理器[{}]执行异常", handler.getClass().getSimpleName(), e);
                Result<Void> result = new Result<>();
                result.setCode(500);
                result.setMsg("处理器执行异常");
                return result;
            }
        }, executorService).orTimeout(HANDLER_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    private String generateCacheKey(FullHttpRequest request, HttpStatement statement) {
        return statement.getServiceId() + ":" + request.uri();
    }

    private void sendError(Channel channel, String message) {
        Result<Void> result = new Result<>();
        result.setCode(500);
        result.setMsg(message);
        channel.writeAndFlush(RequestResultUtil.parse(result));
    }
}