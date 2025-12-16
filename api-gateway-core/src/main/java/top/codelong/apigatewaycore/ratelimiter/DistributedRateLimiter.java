package top.codelong.apigatewaycore.ratelimiter;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 分布式限流器
 * 支持两种模式：
 * 1. DISTRIBUTED（默认）：使用 Redis 侧的滑动窗口和令牌桶算法进行限流
 * 2. LOCAL_DISTRIBUTED：本地限流 + 分布式限流混合模式
 * - 使用令牌桶算法从 Redis 批量获取令牌
 * - 在本地进行高性能限流操作
 */
@Slf4j
@Component
public class DistributedRateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 本地限流器缓存（仅在 LOCAL_DISTRIBUTED 模式下使用）
     * key: 限流目标标识
     * value: Guava RateLimiter
     */
    private final Map<String, RateLimiter> localLimiters = new ConcurrentHashMap<>();

    /**
     * 本地令牌计数器（仅在 LOCAL_DISTRIBUTED 模式下使用）
     * key: 限流目标标识
     * value: 本地剩余令牌数
     */
    private final Map<String, AtomicInteger> localTokenCounters = new ConcurrentHashMap<>();

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

    /**
     * Redis Lua脚本 - 批量获取令牌（用于本地+分布式混合模式）
     */
    private static final String BATCH_GET_TOKENS_SCRIPT =
            """
                    local key = KEYS[1]
                    local batch_size = tonumber(ARGV[1])
                    local limit = tonumber(ARGV[2])
                    local current = tonumber(redis.call('get', key) or '0')
                    local available = math.min(batch_size, limit - current)
                    if available > 0 then
                        redis.call('incrby', key, available)
                        if current == 0 then
                            redis.call('expire', key, 1)
                        end
                        return available
                    else
                        return 0
                    end""";

    public DistributedRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取令牌
     *
     * @param key    限流键
     * @param config 限流配置
     * @return true表示允许通过，false表示被限流
     */
    public boolean tryAcquire(String key, RateLimitConfig config) {
        if (config == null || !config.getEnabled()) {
            return true;
        }

        try {
            String mode = config.getMode() != null ? config.getMode() : "DISTRIBUTED";

            if ("LOCAL_DISTRIBUTED".equals(mode)) {
                // 本地+分布式混合模式
                return tryAcquireLocalDistributed(key, config);
            } else {
                // 默认分布式模式
                return tryAcquireDistributed(key, config);
            }

        } catch (Exception e) {
            log.error("限流异常，降级为放行: key={}", key, e);
            return true; // 异常时放行，保证服务可用性
        }
    }

    /**
     * 分布式限流（默认模式）
     * 直接使用 Redis 侧的滑动窗口或令牌桶算法进行限流
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
     * 本地+分布式混合模式
     * 使用令牌桶算法从 Redis 批量获取令牌，然后在本地进行高性能限流
     */
    private boolean tryAcquireLocalDistributed(String key, RateLimitConfig config) {
        // 获取或创建本地令牌计数器
        AtomicInteger tokenCounter = localTokenCounters.computeIfAbsent(key, k -> new AtomicInteger(0));

        // 尝试消费本地令牌
        int currentTokens = tokenCounter.get();
        if (currentTokens > 0) {
            // 本地有令牌，直接消费
            if (tokenCounter.compareAndSet(currentTokens, currentTokens - 1)) {
                return true;
            }
            // CAS 失败，重试
            return tryAcquireLocalDistributed(key, config);
        }

        // 本地令牌不足，从 Redis 批量获取
        int batchSize = config.getLocalBatchSize() != null ? config.getLocalBatchSize() : 100;
        int acquiredTokens = batchGetTokensFromRedis(key, config, batchSize);

        if (acquiredTokens > 0) {
            // 成功获取令牌，设置本地计数器（减1是因为当前请求消费一个）
            tokenCounter.set(acquiredTokens - 1);
            return true;
        }

        // 无法获取令牌，限流
        return false;
    }

    /**
     * 从 Redis 批量获取令牌
     */
    private int batchGetTokensFromRedis(String key, RateLimitConfig config, int batchSize) {
        try {
            String redisKey = "rate_limit:" + key;
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(BATCH_GET_TOKENS_SCRIPT);
            script.setResultType(Long.class);

            Long result = redisTemplate.execute(
                    script,
                    Collections.singletonList(redisKey),
                    batchSize,
                    config.getLimitCount()
            );

            return result.intValue();
        } catch (Exception e) {
            log.error("从 Redis 批量获取令牌失败: key={}", key, e);
            return 0;
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
        log.info("更新限流配置: key={}, mode={}, config={}", key, config.getMode(), config);
        configCache.put(key, config);

        // 清除旧的本地限流器和令牌计数器
        localLimiters.remove(key);
        localTokenCounters.remove(key);
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
        localTokenCounters.remove(key);
    }

    /**
     * 清空所有限流配置
     */
    public void clearAllConfigs() {
        log.info("清空所有限流配置");
        configCache.clear();
        localLimiters.clear();
        localTokenCounters.clear();
    }

    /**
     * 批量更新限流配置
     */
    public void batchUpdateConfigs(Map<String, RateLimitConfig> configs) {
        log.info("批量更新限流配置，数量: {}", configs.size());
        configs.forEach(this::updateConfig);
    }
}

