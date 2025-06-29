package top.codelong.findsdk.listener;

import jakarta.annotation.Resource;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import top.codelong.findsdk.service.HeartbeatService;

/**
 * 监听Redis消息
 */
@Component
public class RedisMessageListener implements MessageListener {
    @Resource
    private HeartbeatService heartbeatService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        if (channel.equals("heartBeat")) {
            heartbeatService.heartbeat();
        }
    }
}
