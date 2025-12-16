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
import top.codelong.apigatewaycore.ratelimiter.DistributedRateLimiter;
import top.codelong.apigatewaycore.ratelimiter.RateLimitConfig;
import top.codelong.apigatewaycore.socket.BaseHandler;
import top.codelong.apigatewaycore.utils.RequestParameterUtil;
import top.codelong.apigatewaycore.utils.RequestResultUtil;

/**
 * 限流处理器
 * 作为独立的 Handler 集成到 Netty 处理链中
 * 支持多级限流：全局、服务、接口、IP
 * 支持两种限流模式：
 * 1. DISTRIBUTED（默认）：使用 Redis 侧的滑动窗口和令牌桶算法
 * 2. LOCAL_DISTRIBUTED：本地+分布式混合模式，高性能限流
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class RateLimitHandler extends BaseHandler<FullHttpRequest> {
    private static final AttributeKey<HttpStatement> HTTP_STATEMENT_KEY = AttributeKey.valueOf("HttpStatement");

    @Resource
    private DistributedRateLimiter rateLimiter;

    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        HttpStatement httpStatement = channel.attr(HTTP_STATEMENT_KEY).get();
        if (httpStatement == null) {
            sendError(channel, "系统处理异常");
            return;
        }

        try {
            // 1. 全局限流检查
            if (!checkGlobalRateLimit()) {
                sendError(channel, "系统繁忙，请稍后重试");
                return;
            }

            // 2. 服务级限流检查
            String serviceId = httpStatement.getServiceId();
            if (!checkServiceRateLimit(serviceId)) {
                sendError(channel, "服务访问频繁，请稍后重试");
                return;
            }

            // 3. 接口级限流检查
            String url = RequestParameterUtil.getUrl(request);
            if (!checkInterfaceRateLimit(serviceId, url)) {
                sendError(channel, "接口访问频繁，请稍后重试");
                return;
            }

            // 4. IP级限流检查
            String clientIp = getClientIp(request);
            if (!checkIpRateLimit(clientIp)) {
                sendError(channel, "访问过于频繁，请稍后重试");
                return;
            }

            // 所有限流检查通过，继续处理链
            ctx.fireChannelRead(request);

        } catch (Exception e) {
            log.error("限流处理异常", e);
            // 异常时放行，避免影响正常业务
            ctx.fireChannelRead(request);
        }
    }

    /**
     * 全局限流检查
     */
    private boolean checkGlobalRateLimit() {
        String key = "GLOBAL";
        RateLimitConfig config = rateLimiter.getConfig(key);
        if (config == null || !config.getEnabled()) {
            return true;
        }
        return rateLimiter.tryAcquire(key, config);
    }

    /**
     * 服务级限流检查
     */
    private boolean checkServiceRateLimit(String serviceId) {
        String key = "SERVICE:" + serviceId;
        RateLimitConfig config = rateLimiter.getConfig(key);
        if (config == null || !config.getEnabled()) {
            return true;
        }
        return rateLimiter.tryAcquire(key, config);
    }

    /**
     * 接口级限流检查
     */
    private boolean checkInterfaceRateLimit(String serviceId, String url) {
        String key = "INTERFACE:" + serviceId + ":" + url;
        RateLimitConfig config = rateLimiter.getConfig(key);
        if (config == null || !config.getEnabled()) {
            return true;
        }
        return rateLimiter.tryAcquire(key, config);
    }

    /**
     * IP级限流检查
     */
    private boolean checkIpRateLimit(String clientIp) {
        String key = "IP:" + clientIp;
        RateLimitConfig config = rateLimiter.getConfig(key);
        if (config == null || !config.getEnabled()) {
            return true;
        }
        return rateLimiter.tryAcquire(key, config);
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(FullHttpRequest request) {
        // 尝试从X-Forwarded-For获取真实IP
        String xff = request.headers().get("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }

        // 尝试从X-Real-IP获取
        String realIp = request.headers().get("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        // 默认返回unknown
        return "unknown";
    }

    private void sendError(Channel channel, String message) {
        channel.writeAndFlush(RequestResultUtil.parse(Result.error(message)));
    }
}

