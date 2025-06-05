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
    //TODO 消息统一管理
    public void publish(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }
}