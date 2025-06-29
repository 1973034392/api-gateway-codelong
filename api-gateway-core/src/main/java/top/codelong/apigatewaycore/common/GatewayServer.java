package top.codelong.apigatewaycore.common;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.config.GlobalConfiguration;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 网关服务管理类
 * 负责管理可用的网关服务器列表
 */
@Slf4j
@Data
@Component
public class GatewayServer {
    /**
     * 初始化方法，在Bean创建后自动调用
     */
    @PostConstruct
    public void init() {
        log.info("初始化GatewayServer");
        this.update();
    }

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private GlobalConfiguration config;

    // 线程安全的服务器列表
    private CopyOnWriteArrayList<String> servers = new CopyOnWriteArrayList<>();
    // 随机数生成器，用于负载均衡
    private Random random = new Random();

    /**
     * 更新可用服务器列表
     * 从Redis中获取所有活跃的服务器地址
     */
    public void update() {
        log.debug("开始更新服务器列表");
        servers.clear();

        // 从Redis获取所有匹配的服务实例key
        String pattern = "heartbeat:server:" + config.getServerName() + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys.isEmpty()) {
            log.warn("未找到可用的服务器实例");
            return;
        }

        // 解析服务器地址
        for (String key : keys) {
            String[] split = key.split(":");
            if (split.length >= 5) {
                String address = split[3] + ":" + split[4];
                servers.add(address);
                log.debug("添加服务器地址: {}", address);
            }
        }

        log.info("服务器列表更新完成，当前可用服务器数: {}", servers.size());
    }

    /**
     * 随机获取一个可用服务器地址
     * @return 服务器地址(host:port)
     * @throws IllegalStateException 当没有可用服务器时抛出
     */
    public String getOne() {
        if (servers.isEmpty()) {
            log.error("没有可用的服务器");
            throw new IllegalStateException("没有可用的服务器");
        }

        String server = servers.get(random.nextInt(servers.size()));
        log.debug("随机选择服务器: {}", server);
        return server;
    }
}