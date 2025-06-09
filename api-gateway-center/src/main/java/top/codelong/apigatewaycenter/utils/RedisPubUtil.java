package top.codelong.apigatewaycenter.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPubUtil {
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * TODO 刷新本地缓存
     */
    public void ServerFlush(String serverName) {
        redisTemplate.convertAndSend("ServerFlush", serverName);
    }

    /**
     * 发送心跳请求
     */
    public void heartBeat() {
        redisTemplate.convertAndSend("heartBeat", "来自网关中心的心跳请求");
    }
}