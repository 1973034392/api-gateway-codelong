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
import top.codelong.apigatewaycenter.dao.mapper.GatewayGroupDetailMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerDetailMapper;
import top.codelong.apigatewaycenter.utils.NginxConfUtil;
import top.codelong.apigatewaycenter.utils.RedisPubUtil;

@Configuration
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
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        String database = environment.getProperty("spring.data.redis.database");

        Topic topic = new PatternTopic("__keyevent@" + database + "__:expired");

        container.addMessageListener((message, pattern) -> {
            String expiredKey = new String(message.getBody()); // 获取过期的键
            if (expiredKey.contains("heartbeat:server")) {
                String[] s = expiredKey.split(":");
                gatewayServerDetailMapper.offline(s[3] + ":" + s[4]);
                redisPubUtil.ServerFlush();
            } else if (expiredKey.contains("heartbeat:group")) {
                String[] s = expiredKey.split(":");
                gatewayGroupDetailMapper.offline(s[3] + ":" + s[4]);
                nginxConfUtil.removeInstance(s[3] + ":" + s[4]);
            }
        }, topic);

        return container;
    }
}