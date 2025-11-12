package top.codelong.apigatewaycore.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import top.codelong.apigatewaycore.config.listener.RateLimitConfigListener;
import top.codelong.apigatewaycore.config.listener.RedisMessageListener;

/**
 * Redis配置类
 * 配置Redis模板和消息监听容器
 */
@Slf4j
@Configuration
public class RedisConfig {

    /**
     * 创建RedisTemplate Bean
     * @param factory Redis连接工厂
     * @param objectMapper Jackson对象映射器
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, ObjectMapper objectMapper) {
        log.debug("开始配置RedisTemplate");

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 配置Key序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        log.debug("配置Key和HashKey使用String序列化");

        // 配置Value序列化器
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        log.debug("配置Value和HashValue使用Jackson序列化");

        template.afterPropertiesSet();
        log.info("RedisTemplate配置完成");

        return template;
    }

    /**
     * 创建Redis消息监听容器
     * @param factory Redis连接工厂
     * @param listener Redis消息监听器
     * @param rateLimitConfigListener 限流配置监听器
     * @return 配置好的消息监听容器
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory factory,
            RedisMessageListener listener,
            RateLimitConfigListener rateLimitConfigListener) {
        log.debug("开始配置Redis消息监听容器");

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);

        // 添加消息监听器
        container.addMessageListener(listener, new ChannelTopic("heartBeat"));
        log.info("添加消息监听器到heartBeat频道");
        container.addMessageListener(listener, new ChannelTopic("service-launched"));
        log.info("添加消息监听器到server频道");

        // 添加限流配置监听器
        container.addMessageListener(rateLimitConfigListener, new ChannelTopic("rate-limit-config-update"));
        log.info("添加限流配置监听器到rate-limit-config-update频道");

        return container;
    }
}