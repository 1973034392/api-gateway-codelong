package top.codelong.apigatewaycenter.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis发布工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPubUtil {
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 刷新本地缓存
     */
    public void ServerFlush() {
        redisTemplate.convertAndSend("service-launched", "来自网关中心的刷新本地缓存请求");
    }

    /**
     * 发送心跳请求
     */
    public void heartBeat() {
        redisTemplate.convertAndSend("heartBeat", "来自网关中心的心跳请求");
    }
}