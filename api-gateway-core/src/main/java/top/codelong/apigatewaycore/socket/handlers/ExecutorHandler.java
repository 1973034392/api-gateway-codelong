package top.codelong.apigatewaycore.socket.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.*;

/**
 * 执行器处理器
 * 负责根据请求类型选择HTTP或Dubbo连接并执行请求
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ExecutorHandler extends BaseHandler<FullHttpRequest> {
    private static final AttributeKey<HttpStatement> HTTP_STATEMENT_KEY = AttributeKey.valueOf("HttpStatement");

    // 创建自定义线程池，用于处理请求
    private final ExecutorService executorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            Runtime.getRuntime().availableProcessors() * 4,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    // 服务级别的超时配置缓存
    private final ConcurrentHashMap<String, Integer> serviceTimeoutCache = new ConcurrentHashMap<>();

    // 默认超时时间（15秒）
    private static final int DEFAULT_TIMEOUT = 15000;

    @Resource
    private GlobalConfiguration config;
    @Resource
    private GatewayServer gatewayServer;

    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        HttpStatement httpStatement = channel.attr(HTTP_STATEMENT_KEY).get();
        if (httpStatement == null) {
            sendError(channel, "系统处理异常");
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> parameters = RequestParameterUtil.getParameters(request);
                String url = RequestParameterUtil.getUrl(request);

                // 添加URL有效性检查
                if (url == null || url.trim().isEmpty()) {
                    throw new IllegalArgumentException("请求URL不能为空");
                }

                String serverAddr = gatewayServer.getOne();

                // 获取服务特定的超时时间
                int timeout = serviceTimeoutCache.computeIfAbsent(
                        httpStatement.getServiceId(),
                        key -> DEFAULT_TIMEOUT
                );

                BaseConnection connection = createConnection(httpStatement.getIsHttp(), serverAddr, url);

                try {
                    return connection.send(parameters, httpStatement)
                            .get(timeout, TimeUnit.MILLISECONDS);
                } finally {
                    connection.close();
                }
            } catch (TimeoutException e) {
                // 超时后动态调整该服务的超时时间
                adjustTimeout(httpStatement.getServiceId());
                throw new RuntimeException("请求超时");
            } catch (Exception e) {
                log.error("请求执行异常", e);
                throw new RuntimeException(e.getMessage());
            }
        }, executorService).whenComplete((result, throwable) -> {
            // 释放请求资源
            request.release();
            if (throwable != null) {
                sendError(channel, throwable.getMessage());
                return;
            }
            sendResponse(channel, result);
        });
    }

    private BaseConnection createConnection(boolean isHttp, String serverAddr, String url) {
        if (isHttp) {
            return new HTTPConnection(config.getAsyncHttpClient(), "http://" + serverAddr + url);
        } else {
            String dubboAddr = serverAddr.split(":")[0] + ":20880";
            return new DubboConnection(config.getDubboServiceMap(), dubboAddr);
        }
    }

    private void adjustTimeout(String serviceId) {
        serviceTimeoutCache.computeIfPresent(serviceId, (key, oldTimeout) ->
                Math.min(oldTimeout + 1000, 30000) // 增加超时时间，但不超过30秒
        );
    }

    private void sendError(Channel channel, String message) {
        channel.writeAndFlush(RequestResultUtil.parse(Result.error(message)));
    }

    private void sendResponse(Channel channel, Result<?> result) {
        channel.writeAndFlush(RequestResultUtil.parse(result));
    }
}