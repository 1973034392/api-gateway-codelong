package top.codelong.apigatewaycenter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import lombok.extern.slf4j.Slf4j;
import top.codelong.apigatewaycenter.dao.mapper.GatewayGroupDetailMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerDetailMapper;
import top.codelong.apigatewaycenter.utils.NginxConfUtil;
import top.codelong.apigatewaycenter.utils.RedisPubUtil;

/**
 * Redis配置类，用于定义Redis相关的Bean和配置
 * 包括Redis模板配置和Redis消息监听容器配置
 */
@Configuration
@Slf4j  // 使用Lombok的@Slf4j注解生成日志记录器
public class RedisConfig {
    @Resource
    private Environment environment;
    @Resource
    private GatewayServerDetailMapper gatewayServerDetailMapper;
    @Resource
    private GatewayGroupDetailMapper gatewayGroupDetailMapper;
    @Resource
    private NginxConfUtil nginxConfUtil;
    @Resource
    private RedisPubUtil redisPubUtil;

    /**
     * 配置并返回一个RedisTemplate实例
     * 用于操作Redis数据库，设置不同的序列化方式
     *
     * @param factory Redis连接工厂
     * @param objectMapper Jackson对象映射器
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key 和 HashKey 使用 String 序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value 使用 Jackson 序列化
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        log.info("RedisTemplate已成功初始化");
        return template;
    }

    /**
     * 配置并返回一个RedisMessageListenerContainer实例
     * 用于监听Redis键过期事件，并处理服务和组的下线逻辑
     *
     * @param connectionFactory Redis连接工厂
     * @return 配置好的RedisMessageListenerContainer实例
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        String database = environment.getProperty("spring.data.redis.database");

        Topic topic = new PatternTopic("__keyevent@" + database + "__:expired");

        container.addMessageListener((message, pattern) -> {
            String expiredKey = new String(message.getBody()); // 获取过期的键
            log.info("检测到Redis键过期事件: {}", expiredKey);

            if (expiredKey.contains("heartbeat:server")) {
                String[] s = expiredKey.split(":");
                log.info("服务器心跳过期，标记为离线: {}:{}", s[3], s[4]);
                gatewayServerDetailMapper.offline(s[3] + ":" + s[4]);
                redisPubUtil.ServerFlush();
            } else if (expiredKey.contains("heartbeat:group")) {
                String[] s = expiredKey.split(":");
                log.info("分组心跳过期，标记为离线: {}:{}", s[3], s[4]);
                gatewayGroupDetailMapper.offline(s[3] + ":" + s[4]);
            }
        }, topic);

        log.info("RedisMessageListenerContainer已成功初始化");
        return container;
    }
}