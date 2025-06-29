package top.codelong.apigatewaycenter.scheduled;

import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycenter.utils.RedisPubUtil;

/**
 * 心跳定时任务
 */
@Component
public class HeartBeatScheduled {
    @Resource
    private RedisPubUtil redisPubUtil;

    @Scheduled(cron = "0/15 * * * * ?")
    public void heartBeat() {
        redisPubUtil.heartBeat();
    }
}