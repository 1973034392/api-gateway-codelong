package top.codelong.apigatewaycenter.scheduled;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycenter.utils.NginxConfUtil;

@Slf4j
@Component
public class NginxFlushScheduled {
    @Resource
    private NginxConfUtil nginxConfUtil;

    @Scheduled(cron = "0 0/15 * * * ?")
    public void flush() {
        nginxConfUtil.refreshNginxConfig();
    }
}
