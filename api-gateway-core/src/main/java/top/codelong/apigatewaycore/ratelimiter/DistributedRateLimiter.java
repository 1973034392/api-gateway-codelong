package top.codelong.apigatewaycore.ratelimiter;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 分布式限流器
 * 采用本地Guava RateLimiter + Redis分布式限流的多级限流策略
 * 本地限流器承担大部分流量，Redis用于跨节点协调，避免Redis成为瓶颈
 */
@Slf4j
@Component
public class DistributedRateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 本地限流器缓存
     * key: 限流目标标识
     * value: Guava RateLimiter
     */
    private final Map<String, RateLimiter> localLimiters = new ConcurrentHashMap<>();

    /**
     * 限流配置缓存
     */
    private final Map<String, RateLimitConfig> configCache = new ConcurrentHashMap<>();

    /**
     * Redis Lua脚本 - 滑动窗口限流
     */
    private static final String SLIDING_WINDOW_SCRIPT =
            """
                    local key = KEYS[1]
                    local limit = tonumber(ARGV[1])
                    local window = tonumber(ARGV[2])
                    local current = tonumber(ARGV[3])
                    local expire_time = current - window * 1000
                    redis.call('zremrangebyscore', key, 0, expire_time)
                    local count = redis.call('zcard', key)
                    if count < limit then
                        redis.call('zadd', key, current, current)
                        redis.call('expire', key, window + 1)
                        return 1
                    else
                        return 0
                    end""";

    /**
     * Redis Lua脚本 - 令牌桶限流
     */
    private static final String TOKEN_BUCKET_SCRIPT =
            """
                    local key = KEYS[1]
                    local limit = tonumber(ARGV[1])
                    local current = tonumber(redis.call('get', key) or '0')
                    if current < limit then
                        redis.call('incr', key)
                        if current == 0 then
                            redis.call('expire', key, 1)
                        end
                        return 1
                    else
                        return 0
                    end""";

    public DistributedRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取令牌
     *
     * @param key 限流键
     * @param config 限流配置
     * @return true表示允许通过，false表示被限流
     */
    public boolean tryAcquire(String key, RateLimitConfig config) {
        if (config == null || !config.getEnabled()) {
            return true;
        }

        try {
            // 第一层：本地限流（快速失败，保护Redis）
            // 本地限流器设置为配置值的1.2倍，留有余量
            if (!tryAcquireLocal(key, config)) {
                log.debug("本地限流拦截: {}", key);
                return false;
            }

            // 第二层：Redis分布式限流（精确控制）
            boolean allowed = tryAcquireDistributed(key, config);
            if (!allowed) {
                log.debug("分布式限流拦截: {}", key);
            }
            return allowed;

        } catch (Exception e) {
            // 限流器异常时，降级为只使用本地限流
            log.error("分布式限流异常，降级为本地限流: {}", key, e);
            return tryAcquireLocal(key, config);
        }
    }

    /**
     * 本地限流（基于Guava RateLimiter）
     */
    private boolean tryAcquireLocal(String key, RateLimitConfig config) {
        RateLimiter limiter = localLimiters.computeIfAbsent(key, k -> {
            // 本地限流器设置为配置值的1.2倍，避免过度拦截
            double permitsPerSecond = config.getLimitCount() * 1.2 / config.getTimeWindow();
            log.info("创建本地限流器: key={}, permitsPerSecond={}", key, permitsPerSecond);
            return RateLimiter.create(permitsPerSecond);
        });

        // 非阻塞尝试获取令牌
        return limiter.tryAcquire(0, TimeUnit.MILLISECONDS);
    }

    /**
     * Redis分布式限流
     */
    private boolean tryAcquireDistributed(String key, RateLimitConfig config) {
        String redisKey = "rate_limit:" + key;

        if ("TOKEN_BUCKET".equals(config.getStrategy())) {
            return tryAcquireTokenBucket(redisKey, config);
        } else {
            return tryAcquireSlidingWindow(redisKey, config);
        }
    }

    /**
     * 令牌桶算法
     */
    private boolean tryAcquireTokenBucket(String redisKey, RateLimitConfig config) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(TOKEN_BUCKET_SCRIPT);
            script.setResultType(Long.class);

            Long result = redisTemplate.execute(
                script,
                Collections.singletonList(redisKey),
                config.getLimitCount()
            );

            return result == 1L;
        } catch (Exception e) {
            log.error("令牌桶限流执行失败: {}", redisKey, e);
            return true; // 异常时放行
        }
    }

    /**
     * 滑动窗口算法
     */
    private boolean tryAcquireSlidingWindow(String redisKey, RateLimitConfig config) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(SLIDING_WINDOW_SCRIPT);
            script.setResultType(Long.class);

            long currentTime = System.currentTimeMillis();
            Long result = redisTemplate.execute(
                script,
                Collections.singletonList(redisKey),
                config.getLimitCount(),
                config.getTimeWindow(),
                currentTime
            );

            return result == 1L;
        } catch (Exception e) {
            log.error("滑动窗口限流执行失败: {}", redisKey, e);
            return true; // 异常时放行
        }
    }

    /**
     * 更新限流配置
     */
    public void updateConfig(String key, RateLimitConfig config) {
        log.info("更新限流配置: key={}, config={}", key, config);
        configCache.put(key, config);

        // 移除旧的本地限流器，下次访问时会重新创建
        localLimiters.remove(key);
    }

    /**
     * 获取限流配置
     */
    public RateLimitConfig getConfig(String key) {
        return configCache.get(key);
    }

    /**
     * 移除限流配置
     */
    public void removeConfig(String key) {
        log.info("移除限流配置: key={}", key);
        configCache.remove(key);
        localLimiters.remove(key);
    }

    /**
     * 清空所有限流配置
     */
    public void clearAllConfigs() {
        log.info("清空所有限流配置");
        configCache.clear();
        localLimiters.clear();
    }

    /**
     * 批量更新限流配置
     */
    public void batchUpdateConfigs(Map<String, RateLimitConfig> configs) {
        log.info("批量更新限流配置，数量: {}", configs.size());
        configs.forEach(this::updateConfig);
    }
}

