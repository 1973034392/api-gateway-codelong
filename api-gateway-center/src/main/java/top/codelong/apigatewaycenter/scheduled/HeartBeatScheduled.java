package top.codelong.apigatewaycenter.scheduled;

import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycenter.utils.RedisPubUtil;

@Component
public class HeartBeatScheduled {
    @Resource
    private RedisPubUtil redisPubUtil;

    @Scheduled(cron = "0/15 * * * * ?")
    public void heartBeat() {
        redisPubUtil.publish("heartBeat", "来自网关中心的心跳请求");
    }
}