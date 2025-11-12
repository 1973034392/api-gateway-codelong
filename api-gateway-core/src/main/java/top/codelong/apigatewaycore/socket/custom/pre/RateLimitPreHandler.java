package top.codelong.apigatewaycore.socket.custom.pre;

import io.netty.handler.codec.http.FullHttpRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.ratelimiter.DistributedRateLimiter;
import top.codelong.apigatewaycore.ratelimiter.RateLimitConfig;
import top.codelong.apigatewaycore.utils.RequestParameterUtil;

/**
 * 分布式限流前置处理器
 * 支持多级限流：全局、服务、接口、IP
 */
@Slf4j
@Component
@Order(10)
public class RateLimitPreHandler implements CustomPreHandler {

    @Resource
    private DistributedRateLimiter rateLimiter;

    @Override
    public Result<Void> handle(HttpStatement httpStatement, FullHttpRequest request) {
        try {
            // 1. 全局限流
            if (!checkGlobalRateLimit()) {
                return createLimitResult("系统繁忙，请稍后重试");
            }

            // 2. 服务级限流
            String serviceId = httpStatement.getServiceId();
            if (!checkServiceRateLimit(serviceId)) {
                return createLimitResult("服务访问频繁，请稍后重试");
            }

            // 3. 接口级限流
            String url = RequestParameterUtil.getUrl(request);
            if (!checkInterfaceRateLimit(serviceId, url)) {
                return createLimitResult("接口访问频繁，请稍后重试");
            }

            // 4. IP级限流
            String clientIp = getClientIp(request);
            if (!checkIpRateLimit(clientIp)) {
                return createLimitResult("访问过于频繁，请稍后重试");
            }

            return Result.success();

        } catch (Exception e) {
            log.error("限流处理异常", e);
            // 异常时放行，避免影响正常业务
            return Result.success();
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

    /**
     * 创建限流结果
     */
    private Result<Void> createLimitResult(String message) {
        Result<Void> result = new Result<>();
        result.setCode(429); // HTTP 429 Too Many Requests
        result.setMsg(message);
        return result;
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public boolean canRunParallel() {
        // 限流检查需要串行执行，确保准确性
        return false;
    }

    @Override
    public boolean isFailFast() {
        // 限流失败时快速返回
        return true;
    }
}

