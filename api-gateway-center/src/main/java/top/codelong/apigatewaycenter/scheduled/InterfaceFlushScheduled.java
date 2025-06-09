package top.codelong.apigatewaycenter.scheduled;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycenter.dao.mapper.GatewayInterfaceMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayMethodMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerMapper;

@Component
public class InterfaceFlushScheduled {
    @Resource
    private GatewayServerMapper gatewayServerMapper;
    @Resource
    private GatewayInterfaceMapper gatewayInterfaceMapper;
    @Resource
    private GatewayMethodMapper gatewayMethodMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 定时刷新接口信息
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void flushURL() {
        //TODO 根据心跳信息进行刷新接口信息

    }
}
