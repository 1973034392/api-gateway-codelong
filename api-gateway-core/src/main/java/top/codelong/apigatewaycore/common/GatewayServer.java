package top.codelong.apigatewaycore.common;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.config.GlobalConfiguration;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@Component
public class GatewayServer {
    @PostConstruct
    public void init() {
        this.update();
    }

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private GlobalConfiguration config;

    private CopyOnWriteArrayList<String> servers = new CopyOnWriteArrayList<>();
    private Random random = new Random();

    public void update() {
        servers.clear();
        Set<String> keys = redisTemplate.keys("heartbeat:server:" + config.getServerName() + ":*");
        for (String key : keys) {
            String[] split = key.split(":");
            servers.add(split[3] + ":" + split[4]);
        }
    }

    public String getOne() {
        return servers.get(random.nextInt(servers.size()));
    }

}