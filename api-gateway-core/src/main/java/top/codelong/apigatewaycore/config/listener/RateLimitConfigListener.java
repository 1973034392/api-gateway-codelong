package top.codelong.apigatewaycore.config.listener;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.ratelimiter.DistributedRateLimiter;
import top.codelong.apigatewaycore.ratelimiter.RateLimitConfig;

import java.util.Map;
import java.util.Set;

/**
 * 限流配置监听器
 * 监听Redis中的限流配置变更，实时更新本地限流器
 */
@Slf4j
@Component
public class RateLimitConfigListener implements MessageListener {

    @Resource
    private DistributedRateLimiter rateLimiter;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 初始化时加载所有限流配置
     */
    @PostConstruct
    public void init() {
        log.info("初始化限流配置");
        loadAllRateLimitConfigs();
    }

    /**
     * 处理Redis消息
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        log.info("收到限流配置更新消息，频道: {}", channel);

        if ("rate-limit-config-update".equals(channel)) {
            handleConfigUpdate(message);
        }
    }

    /**
     * 处理配置更新消息
     */
    private void handleConfigUpdate(Message message) {
        try {
            String body = new String(message.getBody());
            log.info("限流配置更新内容: {}", body);

            if ("RELOAD_ALL".equals(body)) {
                // 重新加载所有配置
                loadAllRateLimitConfigs();
            } else {
                // 更新单个配置
                RateLimitConfig config = JSON.parseObject(body, RateLimitConfig.class);
                if (config != null) {
                    String key = buildConfigKey(config);
                    rateLimiter.updateConfig(key, config);
                    log.info("限流配置已更新: {}", key);
                }
            }
        } catch (Exception e) {
            log.error("处理限流配置更新失败", e);
        }
    }

    /**
     * 从Redis加载所有限流配置
     */
    private void loadAllRateLimitConfigs() {
        try {
            log.info("开始加载所有限流配置");

            // 清空现有配置
            rateLimiter.clearAllConfigs();

            // 从Redis获取所有限流配置
            Set<String> keys = redisTemplate.keys("rate_limit_config:*");
            if (keys == null || keys.isEmpty()) {
                log.info("未找到限流配置");
                return;
            }

            int count = 0;
            for (String key : keys) {
                try {
                    Map<Object, Object> configMap = redisTemplate.opsForHash().entries(key);
                    if (!configMap.isEmpty()) {
                        RateLimitConfig config = convertToConfig(configMap);
                        if (config != null && config.getEnabled()) {
                            String configKey = buildConfigKey(config);
                            rateLimiter.updateConfig(configKey, config);
                            count++;
                        }
                    }
                } catch (Exception e) {
                    log.error("加载限流配置失败: {}", key, e);
                }
            }

            log.info("限流配置加载完成，共加载 {} 条配置", count);
        } catch (Exception e) {
            log.error("加载所有限流配置失败", e);
        }
    }

    /**
     * 将Map转换为RateLimitConfig
     */
    private RateLimitConfig convertToConfig(Map<Object, Object> configMap) {
        try {
            RateLimitConfig.RateLimitConfigBuilder builder = RateLimitConfig.builder()
                .id(Long.valueOf(configMap.get("id").toString()))
                .ruleName((String) configMap.get("ruleName"))
                .limitType((String) configMap.get("limitType"))
                .limitTarget((String) configMap.get("limitTarget"))
                .limitCount(Integer.valueOf(configMap.get("limitCount").toString()))
                .timeWindow(Integer.valueOf(configMap.get("timeWindow").toString()))
                .enabled(Boolean.valueOf(configMap.get("enabled").toString()))
                .strategy((String) configMap.get("strategy"));

            // 处理新增的配置字段
            Object mode = configMap.get("mode");
            if (mode != null) {
                builder.mode((String) mode);
            }

            Object localBatchSize = configMap.get("localBatchSize");
            if (localBatchSize != null) {
                builder.localBatchSize(Integer.valueOf(localBatchSize.toString()));
            }

            Object localCapacityMultiplier = configMap.get("localCapacityMultiplier");
            if (localCapacityMultiplier != null) {
                builder.localCapacityMultiplier(Double.valueOf(localCapacityMultiplier.toString()));
            }

            return builder.build();
        } catch (Exception e) {
            log.error("转换限流配置失败", e);
            return null;
        }
    }

    /**
     * 构建配置键
     */
    private String buildConfigKey(RateLimitConfig config) {
        String limitType = config.getLimitType();
        String limitTarget = config.getLimitTarget();

        if ("GLOBAL".equals(limitType)) {
            return "GLOBAL";
        } else if ("SERVICE".equals(limitType)) {
            return "SERVICE:" + limitTarget;
        } else if ("INTERFACE".equals(limitType)) {
            return "INTERFACE:" + limitTarget;
        } else if ("IP".equals(limitType)) {
            return "IP:" + limitTarget;
        }

        return limitType + ":" + limitTarget;
    }
}

